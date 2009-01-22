package com.carbonfive.testutils.dbunit;

import org.dbunit.*;
import org.dbunit.database.*;
import org.springframework.jdbc.datasource.*;

import javax.sql.*;

/**
 * A specialize DbUnit DatabaseTester which plays nicely with Spring by using the connection which is associated with the current transaction (if there is one)
 * and leaving the connection open.
 */
public class ExistingConnectionDatabaseTester extends AbstractDatabaseTester
{

    private DataSource dataSource;

    public ExistingConnectionDatabaseTester(DataSource dataSource)
    {
        super();
        this.dataSource = dataSource;
    }

    public IDatabaseConnection getConnection() throws Exception
    {
        return new DatabaseConnection(DataSourceUtils.getConnection(dataSource));
    }

    @Override
    public void closeConnection(IDatabaseConnection connection) throws Exception
    {
        // Don't close that connection!
    }

}
