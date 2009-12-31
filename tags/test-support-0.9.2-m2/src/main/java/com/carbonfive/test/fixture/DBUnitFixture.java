package com.carbonfive.test.fixture;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import java.net.URL;
import java.sql.Connection;

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
            return new FlatXmlDataSetBuilder().build(dataUrl);
        }
        catch (Exception e)
        {
            throw new FixtureException("Error loading DBUnit dataset", e);
        }
    }
}
