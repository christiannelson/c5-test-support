package com.carbonfive.testutils.functional;

import com.carbonfive.testutils.dbunit.DBUnitUtils;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.installer.Installer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;

public class FunctionalTestRunner extends BlockJUnit4ClassRunner
{
    private static Logger logger = LoggerFactory.getLogger(FunctionalTestRunner.class);

    private static boolean initialized = false;
    private static boolean databaseDirty = true;

    private static DataSource dataSource = null;
    private static InstalledLocalContainer container = null;

    public FunctionalTestRunner(Class<?> klass) throws Exception
    {
        super(klass);

        synchronized (FunctionalTestRunner.class)
        {
            if (!initialized)
            {
                String propertiesLocation = "classpath:/functional-tests.properties";

                if (klass.isAnnotationPresent(FunctionalTestConfiguration.class))
                {
                    propertiesLocation = klass.getAnnotation(FunctionalTestConfiguration.class).value();
                }

                FunctionalTestProperties.load(propertiesLocation);

                final String jdbcDriverClass = FunctionalTestProperties.get().getProperty("db.driver");
                final String jdbcUrl = FunctionalTestProperties.get().getProperty("db.url");
                final String jdbcUsername = FunctionalTestProperties.get().getProperty("db.username");
                final String jdbcPassword = FunctionalTestProperties.get().getProperty("db.password");

                if (isNotBlank(jdbcDriverClass) && isNotBlank(jdbcUrl))
                {
                    Driver driver = (Driver) BeanUtils.instantiateClass(Class.forName(jdbcDriverClass));
                    dataSource = new SimpleDriverDataSource(driver, jdbcUrl, jdbcUsername, jdbcPassword);
                }

                initialized = true;
            }
        }
    }

    @Override
    protected Statement withBeforeClasses(Statement statement)
    {
        Statement junitBeforeClasses = super.withBeforeClasses(statement);
        return new RunBeforeTestClassCallbacks(junitBeforeClasses);
    }

    @Override
    protected Statement withAfterClasses(Statement statement)
    {
        Statement junitAfterClasses = super.withAfterClasses(statement);
        return new RunAfterTestClassCallbacks(junitAfterClasses, getTestClass().getJavaClass());
    }

    @Override
    protected Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance, Statement statement)
    {
        Statement junitBefores = super.withBefores(frameworkMethod, testInstance, statement);
        return new RunBeforeTestMethodCallbacks(junitBefores);
    }

    @Override
    protected Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance, Statement statement)
    {
        Statement junitAfters = super.withAfters(frameworkMethod, testInstance, statement);
        return new RunAfterTestMethodCallbacks(junitAfters, frameworkMethod.getMethod());
    }

    private void loadDatabaseFixture() throws Exception
    {
        // TODO Allow for custom fixture loading.

        databaseDirty = false;

        if (isBlank(FunctionalTestProperties.get().getProperty("fixture.file")))
        {
            return;
        }

        logger.info("Loading database fixture data.");

        Resource fixture = new DefaultResourceLoader().getResource(FunctionalTestProperties.get().getProperty("fixture.file"));

        //if (!fixture.exists())
        //{
        //    logger.error(format("Fixture file '%s' doesn't exist.", fixture.getFilename()));
        //    return;
        //}

        ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(fixture.getInputStream()));
        dataSet.addReplacementObject("[NULL]", null);

        Connection connection = dataSource.getConnection();

        DefaultDatabaseTester tester = new DefaultDatabaseTester(new DatabaseConnection(connection));
        tester.getConnection().getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, DBUnitUtils.determineDataTypeFactory(connection));
        tester.setDataSet(dataSet);
        tester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);

        tester.onSetup();
        tester.getConnection().close();
    }

    private void startApplication() throws MalformedURLException
    {
        // TODO Allow developer to run tests against an alfready running app server (sys property).

        logger.info("Starting application server.");

        final String appserverInstaller = FunctionalTestProperties.get().getProperty("appserver.installer");
        final String appserverContainer = FunctionalTestProperties.get().getProperty("appserver.container");
        final String appserverPort = FunctionalTestProperties.get().getProperty("appserver.port", "8080");

        // (1) Optional step to install the container from a URL pointing to its distribution
        Installer installer = new ZipURLInstaller(new URL(appserverInstaller));
        installer.install();

        // (2) Create the Cargo Container instance wrapping our physical container
        LocalConfiguration configuration = (LocalConfiguration) new DefaultConfigurationFactory()
                .createConfiguration(appserverContainer, ContainerType.INSTALLED, ConfigurationType.STANDALONE);
        configuration.setProperty("cargo.servlet.port", appserverPort);

        container = (InstalledLocalContainer) new DefaultContainerFactory().createContainer(appserverContainer, ContainerType.INSTALLED, configuration);
        //container.setOutput("target/output.log");
        //container.setLogger(new SimpleLogger());
        container.setHome(installer.getHome());

        Map props = new HashMap(FunctionalTestProperties.get().size());
        for (Object key : FunctionalTestProperties.get().keySet())
        {
            props.put(key, FunctionalTestProperties.get().get(key));
        }
        container.setSystemProperties(props);

        // (3) Statically deploy some WAR (optional)
        // TODO Exploded war directory support.
        final String appWar = FunctionalTestProperties.get().getProperty("app.war");
        final String appContext = FunctionalTestProperties.get().getProperty("app.context");

        WAR deployable = new WAR(appWar);
        deployable.setContext(appContext);
        configuration.addDeployable(deployable);

        // (4) Start the container
        container.start();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                if (container != null)
                {
                    logger.info("Shutting down application server.");
                    container.stop();
                    container = null;
                }
            }
        });
    }

    private class RunBeforeTestClassCallbacks extends Statement
    {
        private Statement junitBeforeClasses;

        public RunBeforeTestClassCallbacks(Statement junitBeforeClasses)
        {
            this.junitBeforeClasses = junitBeforeClasses;
        }

        @Override
        public void evaluate() throws Throwable
        {
            if (container == null)
            {
                loadDatabaseFixture();

                startApplication();
            }

            junitBeforeClasses.evaluate();
        }
    }

    private class RunAfterTestClassCallbacks extends Statement
    {
        private Statement junitAfterClasses;
        private Class<?> testClass;

        public RunAfterTestClassCallbacks(Statement junitAfterClasses, Class<?> testClass)
        {
            this.junitAfterClasses = junitAfterClasses;
            this.testClass = testClass;
        }

        @Override
        public void evaluate() throws Throwable
        {
            junitAfterClasses.evaluate();

            if (testClass.isAnnotationPresent(DirtiesDatabase.class))
            {
                logger.info("Test class dirties the database, marking the database dirty.");
                databaseDirty = true;
            }
        }
    }

    private class RunBeforeTestMethodCallbacks extends Statement
    {
        private Statement junitBefores;

        public RunBeforeTestMethodCallbacks(Statement junitBefores)
        {
            this.junitBefores = junitBefores;
        }

        @Override
        public void evaluate() throws Throwable
        {
            if (databaseDirty)
            {
                final boolean restartApplication = Boolean.parseBoolean(FunctionalTestProperties.get().getProperty("fixture.restart_application", "false"));

                String msg = null;

                if (restartApplication)
                {
                    logger.info("Previous test dirtied the database, reloading fixture and restarting application server.");
                    container.stop();
                }
                else
                {
                    msg = "Previous test dirtied the database, reloading fixture.";
                    logger.info(msg);
                }

                loadDatabaseFixture();
                databaseDirty = false;

                if (restartApplication)
                {
                    logger.info("Starting application server.");
                    container.start();
                }
            }

            junitBefores.evaluate();
        }
    }

    private class RunAfterTestMethodCallbacks extends Statement
    {
        private Statement junitAfters;
        private Method method;

        public RunAfterTestMethodCallbacks(Statement junitAfters, Method method)
        {
            this.junitAfters = junitAfters;
            this.method = method;
        }

        @Override
        public void evaluate() throws Throwable
        {
            junitAfters.evaluate();

            if (method.isAnnotationPresent(DirtiesDatabase.class))
            {
                logger.info("Test dirties the database, marking the database dirty.");
                databaseDirty = true;
            }
        }
    }
}
