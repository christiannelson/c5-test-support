package com.carbonfive.test.functional;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.installer.Installer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.util.log.SimpleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CargoApplicationServerManager implements ApplicationServerManager
{
    private Logger logger = LoggerFactory.getLogger(CargoApplicationServerManager.class);

    private Properties properties;

    private static InstalledLocalContainer container = null;

    public void initialize(Properties properties)
    {
        this.properties = properties;

        final String appserverInstaller = FunctionalTestProperties.get().getProperty("appserver.installer");

        // (1) Optional step to install the container from a URL pointing to its distribution
        Installer installer = null;
        try
        {
            installer = new ZipURLInstaller(new URL(appserverInstaller));
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        installer.install();

        final String appserverContainer = FunctionalTestProperties.get().getProperty("appserver.container");
        final String appserverPort = properties.getProperty("appserver.port", "8080");

        // (2) Create the Cargo Container instance wrapping our physical container
        LocalConfiguration configuration = (LocalConfiguration) new DefaultConfigurationFactory()
                .createConfiguration(appserverContainer, ContainerType.INSTALLED, ConfigurationType.STANDALONE);
        configuration.setProperty("cargo.servlet.port", appserverPort);

        container = (InstalledLocalContainer) new DefaultContainerFactory().createContainer(appserverContainer, ContainerType.INSTALLED, configuration);

        if (Boolean.valueOf(properties.getProperty("appserver.logging", "false")))
        {
            container.setLogger(new SimpleLogger());
        }

        container.setHome(installer.getHome());

        Map<Object, Object> props = new HashMap<Object, Object>(properties.size());
        for (Object key : properties.keySet())
        {
            props.put(key, properties.get(key));
        }
        container.setSystemProperties(props);

        // (3) Statically deploy some WAR (optional)
        // TODO Exploded war directory support.
        final String appWar = properties.getProperty("app.war");
        final String appContext = properties.getProperty("app.context");

        WAR deployable = new WAR(appWar);
        deployable.setContext(appContext);
        configuration.addDeployable(deployable);

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

    public void start()
    {
        final String appserverContainer = properties.getProperty("appserver.container");
        final String appserverPort = properties.getProperty("appserver.port", "8080");

        logger.info(format("Starting application server '%s' on port %s.", appserverContainer, appserverPort));

        // (4) Start the container
        container.start();
    }

    public void stop()
    {
        container.stop();
    }
}
