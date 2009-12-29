package com.carbonfive.test.spring.dbunit;

import com.carbonfive.test.dbunit.DBUnitUtils;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Constants;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import static org.springframework.util.ClassUtils.getPackageName;
import static org.springframework.util.ClassUtils.getQualifiedName;

import javax.sql.DataSource;
import static java.lang.String.format;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Spring test framework TestExecutionListener which looks for the DataSet annotation and if found, attempts to load a data set (test fixture) before the test
 * is run.
 */
public class DataSetTestExecutionListener extends AbstractTestExecutionListener
{
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<Method, DatasetConfiguration> configurationCache = Collections.synchronizedMap(new IdentityHashMap<Method, DatasetConfiguration>());
    private final static Constants databaseOperations = new Constants(DatabaseOperation.class);

    static
    {
        ClasspathURLHandler.register();
    }

    public void beforeTestMethod(TestContext testContext) throws Exception
    {
        // Determine if a dataset should be loaded, from where, and extract any special configuration.
        DatasetConfiguration configuration = determineConfiguration(testContext.getTestClass(), testContext.getTestMethod());

        if (configuration == null || configuration.getLocation() == null) { return; }

        configurationCache.put(testContext.getTestMethod(), configuration);

        // Find a single, unambiguous data source.
        DataSource dataSource = lookupDataSource(testContext);

        // Fetch a connection from the data source, using an existing one if we're already participating in a transaction.
        Connection connection = DataSourceUtils.getConnection(dataSource);
        configuration.setConnectionTransactional(DataSourceUtils.isConnectionTransactional(connection, dataSource));

        // Load the data set.
        try
        {
            loadData(configuration, connection);
        }
        catch (DataSetException e)
        {
            if (!configuration.isConnectionTransactional())
            {
                connection.close();
            }
            throw e;
        }
    }

    public void afterTestMethod(TestContext testContext) throws Exception
    {
        DatasetConfiguration configuration = configurationCache.get(testContext.getTestMethod());

        if (configurationCache.remove(testContext.getTestMethod()) == null) { return; }

        if (log.isInfoEnabled())
        {
            log.info(format("Tearing down dataset using operation '%s', %s.", configuration.getTeardownOperation(),
                            configuration.isConnectionTransactional() ? "leaving database connection open" : "closing database connection"));
        }

        configuration.getDatabaseTester().onTearDown();

        if (!configuration.isConnectionTransactional())
        {
            try
            {
                configuration.getDatabaseTester().getConnection().close();
            }
            catch (Exception e)
            {
                // do nothing: this connection is associated with an active transaction and we assume the framework will close the connection.
            }
        }
    }

    protected void configureReplacementDataSet(ReplacementDataSet dataSet)
    {
        // Subclasses can add additional replacements.
    }

    protected void configureDatabaseConfig(DatabaseConfig config)
    {
        // Subclasses can further configure dbunit.
    }

    DatasetConfiguration determineConfiguration(Class<?> testClass, Method testMethod)
    {
        DataSet annotation = (DataSet) findAnnotation(testMethod, DataSet.class);

        if (annotation == null) { return null; }

        DatasetConfiguration configuration = new DatasetConfiguration();

        // Dataset source value.
        String location = annotation.value();

        if (location != null)
        {
            if ("".equals(location))
            {
                location = "classpath:/" + getQualifiedName(testClass).replace('.', '/') + ".xml";
            }
            else if (!location.contains(":") && !location.contains("/"))
            {
                location = "classpath:/" + getPackageName(testClass).replace('.', '/') + "/" + location;
            }
        }

        configuration.setLocation(location);

        // Setup and teardown operations.
        if (isNotBlank(annotation.setupOperation()))
        {
            configuration.setSetupOperation(annotation.setupOperation());
        }

        if (isNotBlank(annotation.teardownOperation()))
        {
            configuration.setTeardownOperation(annotation.teardownOperation());
        }

        return configuration;
    }

    /**
     * Looks for a single, unambiguous datasource in the test's application context.
     *
     * @param testContext the current test context
     * @return the only datasource in the current application context
     */
    DataSource lookupDataSource(TestContext testContext)
    {
        String[] dsNames = testContext.getApplicationContext().getBeanNamesForType(DataSource.class);
        if (dsNames.length != 1)
        {
            final String s = "A single, unambiguous DataSource must be defined in the application context.";
            log.error(s);
            throw new IllegalStateException(s);
        }
        return (DataSource) testContext.getApplicationContext().getBean(dsNames[0]);
    }

    /**
     * Given the location of the dataset and the datasource.
     *
     * @param configuration the spring-style resource location of the dataset to be loaded
     * @param connection    the connection to use for loading the dataset
     * @throws Exception if an error occurs when loading the dataset
     */
    void loadData(DatasetConfiguration configuration, Connection connection) throws Exception
    {
        if (log.isInfoEnabled())
        {
            log.info(format("Loading dataset from location '%s' using operation '%s'.", configuration.getLocation(), configuration.getSetupOperation()));
        }


        InputStream is = new DefaultResourceLoader().getResource(configuration.getLocation()).getInputStream();
        ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(is));
        dataSet.addReplacementObject("[NULL]", null);
        configureReplacementDataSet(dataSet);

        IDatabaseTester tester = new DefaultDatabaseTester(new DatabaseConnection(connection)
        {
            public void close() throws SQLException
            {
                // do nothing: this will be closed later if necessary.
            }
        });

        tester.getConnection().getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, DBUnitUtils.determineDataTypeFactory(connection));
        configureDatabaseConfig(tester.getConnection().getConfig());

        configuration.setDatabaseTester(tester);

        tester.setDataSet(dataSet);
        tester.setSetUpOperation((DatabaseOperation) databaseOperations.asObject(configuration.getSetupOperation()));
        tester.setTearDownOperation((DatabaseOperation) databaseOperations.asObject(configuration.getTeardownOperation()));
        tester.onSetup();
    }

    Annotation findAnnotation(Method method, Class<? extends Annotation> annotationType)
    {
        Annotation annotation = AnnotationUtils.findAnnotation(method, annotationType);
        return annotation == null ? AnnotationUtils.findAnnotation(method.getDeclaringClass(), annotationType) : annotation;
    }

    static class DatasetConfiguration
    {
        private String location;
        private String setupOperation;
        private String teardownOperation;
        private IDatabaseTester databaseTester;
        private boolean connectionTransactional;

        public String getLocation()
        {
            return location;
        }

        public void setLocation(String location)
        {
            this.location = location;
        }

        public String getSetupOperation()
        {
            return setupOperation;
        }

        public void setSetupOperation(String setupOperation)
        {
            this.setupOperation = setupOperation;
        }

        public String getTeardownOperation()
        {
            return teardownOperation;
        }

        public void setTeardownOperation(String teardownOperation)
        {
            this.teardownOperation = teardownOperation;
        }

        public IDatabaseTester getDatabaseTester()
        {
            return databaseTester;
        }

        public void setDatabaseTester(IDatabaseTester databaseTester)
        {
            this.databaseTester = databaseTester;
        }

        public boolean isConnectionTransactional()
        {
            return connectionTransactional;
        }

        public void setConnectionTransactional(boolean connectionTransactional)
        {
            this.connectionTransactional = connectionTransactional;
        }
    }
}
