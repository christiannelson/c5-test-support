package com.carbonfive.test.functional;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Method;
import java.util.Properties;

public class FunctionalTestRunner extends BlockJUnit4ClassRunner
{
    public static final String DEFAULT_DATABASE_FIXTURE_LOADER = "com.carbonfive.test.functional.DBUnitDatabaseFixtureLoader";
    public static final String DEFAULT_APPLICATION_SERVER_MANAGER = "com.carbonfive.test.functional.CargoApplicationServerManager";

    private static Logger logger = LoggerFactory.getLogger(FunctionalTestRunner.class);

    private static boolean configured = false;
    private static boolean initialized = false;
    private static boolean databaseDirty = true;

    private static DatabaseFixtureLoader fixtureLoader;
    private static ApplicationServerManager serverManager;

    public FunctionalTestRunner(Class<?> klass) throws Exception
    {
        super(klass);

        synchronized (FunctionalTestRunner.class)
        {
            if (!configured)
            {
                String propertiesLocation = "classpath:/functional-tests.properties";

                if (klass.isAnnotationPresent(FunctionalTestConfiguration.class))
                {
                    propertiesLocation = klass.getAnnotation(FunctionalTestConfiguration.class).value();
                }

                FunctionalTestProperties.load(propertiesLocation);

                try
                {
                    String className = FunctionalTestProperties.get().getProperty("fixture.loader.classname", DEFAULT_DATABASE_FIXTURE_LOADER);

                    fixtureLoader = (DatabaseFixtureLoader) BeanUtils.instantiateClass(Class.forName(className));
                    fixtureLoader.initialize(FunctionalTestProperties.get());
                }
                catch (ClassNotFoundException e)
                {
                    logger.error("Failed to initialize the database fixture loader.", e);
                    throw e;
                }

                try
                {
                    if (System.getProperty("useExternalApplication") != null) // TODO Better name for this?
                    {
                        serverManager = new NoopApplicationServerManager();
                    }
                    else
                    {
                        String className = FunctionalTestProperties.get().getProperty("appserver.manager.classname", DEFAULT_APPLICATION_SERVER_MANAGER);
                        serverManager = (ApplicationServerManager) BeanUtils.instantiateClass(Class.forName(className));
                    }

                    serverManager.initialize(FunctionalTestProperties.get());
                }
                catch (Exception e)
                {
                    logger.error("Failed to initialize the application server manager.", e);
                    throw e;
                }

                configured = true;
            }
        }
    }

    @Override
    protected Statement withBeforeClasses(Statement statement)
    {
        Statement junitBeforeClasses = super.withBeforeClasses(statement);
        return new RunBeforeTestClassCallbacks(junitBeforeClasses, getTestClass().getJavaClass());
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
        return new RunBeforeTestMethodCallbacks(junitBefores, getTestClass().getJavaClass(), frameworkMethod.getMethod());
    }

    @Override
    protected Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance, Statement statement)
    {
        Statement junitAfters = super.withAfters(frameworkMethod, testInstance, statement);
        return new RunAfterTestMethodCallbacks(junitAfters, frameworkMethod.getMethod());
    }

    private void loadDatabaseFixture(Class<?> testClass, Method testMethod) throws Exception
    {
        databaseDirty = false;

        if (fixtureLoader == null) { return; }

        fixtureLoader.load(testClass, testMethod);
    }

    private class RunBeforeTestClassCallbacks extends Statement
    {
        private Statement junitBeforeClasses;
        private Class<?> testClass;

        public RunBeforeTestClassCallbacks(Statement junitBeforeClasses, Class<?> testClass)
        {
            this.junitBeforeClasses = junitBeforeClasses;
            this.testClass = testClass;
        }

        @Override
        public void evaluate() throws Throwable
        {
            if (!initialized)
            {
                initialized = true;
                
                loadDatabaseFixture(testClass, null);

                serverManager.start();
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
        private Class<?> testClass;
        private Method testMethod;

        public RunBeforeTestMethodCallbacks(Statement junitBefores, Class<?> testClass, Method testMethod)
        {
            this.junitBefores = junitBefores;
            this.testClass = testClass;
            this.testMethod = testMethod;
        }

        @Override
        public void evaluate() throws Throwable
        {
            if (databaseDirty)
            {
                final boolean restartApplication = Boolean.parseBoolean(FunctionalTestProperties.get().getProperty("fixture.restart_application", "false"));

                if (restartApplication)
                {
                    logger.info("Previous test dirtied the database, reloading fixture and restarting application server.");
                    serverManager.stop();
                }
                else
                {
                    logger.info("Previous test dirtied the database, reloading fixture.");
                }

                loadDatabaseFixture(testClass, testMethod);

                if (restartApplication)
                {
                    logger.info("Starting application server.");
                    serverManager.start();
                }
            }

            junitBefores.evaluate();
        }
    }

    private class RunAfterTestMethodCallbacks extends Statement
    {
        private Statement junitAfters;
        private Method testMethod;

        public RunAfterTestMethodCallbacks(Statement junitAfters, Method testMethod)
        {
            this.junitAfters = junitAfters;
            this.testMethod = testMethod;
        }

        @Override
        public void evaluate() throws Throwable
        {
            junitAfters.evaluate();

            if (testMethod.isAnnotationPresent(DirtiesDatabase.class))
            {
                logger.info("Test dirties the database, marking the database dirty.");
                databaseDirty = true;
            }
        }
    }

    private class NoopApplicationServerManager implements ApplicationServerManager
    {
        public void initialize(Properties properties)
        {
        }

        public void start()
        {
        }

        public void stop()
        {
        }
    }
}