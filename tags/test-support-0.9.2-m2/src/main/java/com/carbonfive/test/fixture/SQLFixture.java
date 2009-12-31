package com.carbonfive.test.fixture;

import com.carbonfive.db.jdbc.DatabaseUtils;
import com.carbonfive.db.jdbc.ScriptRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;

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
