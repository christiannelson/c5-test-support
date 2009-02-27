package com.carbonfive.testutils.fixture;

import com.carbonfive.db.migration.*;
import com.carbonfive.testutils.*;
import static org.junit.Assert.*;
import org.junit.*;

import javax.sql.*;
import java.io.*;
import java.sql.*;

public class SQLFixtureTest
{
    private DataSource dataSource;

    public SQLFixtureTest()
    {
        this.dataSource = DatabaseTestUtils.createUniqueDataSource();
    }

    @Before
    public void migrate() throws Exception
    {
        new DataSourceMigrationManager(dataSource).migrate();
    }

    @Test
    public void testLoadFromScript() throws Exception
    {
        Reader reader = new InputStreamReader(getClass().getResourceAsStream("/db/fixtures/books.sql"));
        Connection connection = dataSource.getConnection();
        new SQLFixture(reader).load(connection);

        try
        {
            ResultSet resultSet = connection.createStatement().executeQuery("select count(*) from book");

            assertTrue(resultSet.next());
            assertEquals(2, resultSet.getInt(1));
        }
        finally
        {
            connection.close();
        }
    }

    @Test
    public void testLoadFromString() throws Exception
    {
        Reader reader = new StringReader("delete from book;\ninsert into book values (1, '0307387895', 'The Road', 'Cormac McCarthy');");
        Connection connection = dataSource.getConnection();
        new SQLFixture(reader).load(connection);

        try
        {
            ResultSet resultSet = connection.createStatement().executeQuery("select count(*) from book");

            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt(1));
        }
        finally
        {
            connection.close();
        }
    }
}