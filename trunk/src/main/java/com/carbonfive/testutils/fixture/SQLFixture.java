package com.carbonfive.testutils.fixture;

import com.carbonfive.db.jdbc.*;

import java.io.*;
import java.sql.*;

/**
 * SqlFixture runs a SQL script to populate a database with data.
 */
public class SQLFixture implements Fixture
{
    private Reader reader;

    public SQLFixture(Reader reader)
    {
        this.reader = reader;
    }

    public SQLFixture(InputStream in)
    {
        try
        {
            this.reader = new InputStreamReader(in, "UTF-8");
        }
        catch (UnsupportedEncodingException ignored)
        {
        }
    }

    public void load(Connection connection) throws FixtureException
    {
        try
        {
            ScriptRunner runner = new ScriptRunner(DatabaseUtils.databaseType(connection.getMetaData().getURL()));
            runner.execute(connection, reader);
        }
        catch (Exception e)
        {
            throw new FixtureException("Error running fixture SQL script", e);
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }
}
