package com.carbonfive.test.fixture;

import com.carbonfive.db.migration.DataSourceMigrationManager;
import com.carbonfive.test.DatabaseTestUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

public class DBUnitFixtureTest
{
    private DataSource dataSource;

    public DBUnitFixtureTest()
    {
        this.dataSource = DatabaseTestUtils.createUniqueDataSource();
    }

    @Before
    public void migrate() throws Exception
    {
        new DataSourceMigrationManager(dataSource).migrate();
    }

    @Test
    public void testLoad() throws Exception
    {
        Connection connection = dataSource.getConnection();
        new DBUnitFixture("/db/fixtures/books.xml").load(connection);

        try
        {
            ResultSet resultSet = connection.createStatement().executeQuery("select count(*) from book");

            assertTrue(resultSet.next());
            assertEquals(3, resultSet.getInt(1));
        }
        finally
        {
            connection.close();
        }
    }
}
