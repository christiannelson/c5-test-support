package com.carbonfive.testutils.fixture;

import org.dbunit.database.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.xml.*;
import org.dbunit.operation.*;

import java.net.*;
import java.sql.*;

/**
 * DbUnitFixture loads data into a database from a DBUnit XML file.
 */
public class DBUnitFixture implements Fixture
{
    private String location;

    public DBUnitFixture(String location)
    {
        this.location = location;
    }

    public void load(Connection connection) throws FixtureException
    {
        try
        {
            IDatabaseConnection conn = new DatabaseConnection(connection);
            IDataSet data = getDataSet();
            DatabaseOperation.CLEAN_INSERT.execute(conn, data);
        }
        catch (Exception e)
        {
            throw new FixtureException("Error loading DBUnit fixture", e);
        }
    }

    protected IDataSet getDataSet() throws FixtureException
    {
        try
        {
            URL dataUrl = getClass().getResource(location);
            return new FlatXmlDataSet(dataUrl);
        }
        catch (Exception e)
        {
            throw new FixtureException("Error loading DBUnit dataset", e);
        }
    }
}
