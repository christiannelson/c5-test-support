package com.carbonfive.test.functional;

import com.carbonfive.test.dbunit.DBUnitUtils;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class DBUnitDatabaseFixtureLoader implements DatabaseFixtureLoader
{
    private Logger logger = LoggerFactory.getLogger(DBUnitDatabaseFixtureLoader.class);

    private Properties properties;
    private DataSource dataSource;

    public void initialize(Properties properties) throws ClassNotFoundException
    {
        this.properties = properties;

        final String jdbcDriverClass = FunctionalTestProperties.get().getProperty("db.driver");
        final String jdbcUrl = FunctionalTestProperties.get().getProperty("db.url");
        final String jdbcUsername = FunctionalTestProperties.get().getProperty("db.username");
        final String jdbcPassword = FunctionalTestProperties.get().getProperty("db.password");

        if (isNotBlank(jdbcDriverClass) && isNotBlank(jdbcUrl))
        {
            Driver driver = (Driver) BeanUtils.instantiateClass(Class.forName(jdbcDriverClass));
            dataSource = new SimpleDriverDataSource(driver, jdbcUrl, jdbcUsername, jdbcPassword);
        }
    }

    public void load(Class<?> testClass, Method testMethod) throws DatabaseFixtureException
    {
        if (dataSource == null || isBlank(properties.getProperty("fixture.file")))
        {
            return;
        }

        logger.info("Loading database fixture data.");

        final Resource fixture = new DefaultResourceLoader().getResource(properties.getProperty("fixture.file"));

        //if (!fixture.exists())
        //{
        //    logger.error(format("Fixture file '%s' doesn't exist.", fixture.getFilename()));
        //    return;
        //}

        new JdbcTemplate(dataSource).execute(new ConnectionCallback<Object>()
        {
            public Object doInConnection(Connection connection) throws SQLException, DataAccessException
            {
                try
                {
                    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(fixture.getInputStream()));
                    dataSet.addReplacementObject("[NULL]", null);

                    IDatabaseConnection cxn = new DatabaseConnection(connection);
                    cxn.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, DBUnitUtils.determineDataTypeFactory(connection));
                    DatabaseOperation.CLEAN_INSERT.execute(cxn, dataSet);
                }
                catch (Exception e)
                {
                    throw new DatabaseFixtureException("Failed to apply DBUnit database fixture.", e);
                }
                return null;
            }
        });
    }
}
