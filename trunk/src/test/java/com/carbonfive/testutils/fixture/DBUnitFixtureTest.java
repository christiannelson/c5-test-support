package com.carbonfive.testutils.fixture;

import com.carbonfive.db.migration.*;
import com.carbonfive.testutils.*;
import static org.junit.Assert.*;
import org.junit.*;

import javax.sql.*;
import java.sql.*;

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
