package com.carbonfive.test;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

public class DatabaseTestUtils
{
    public static int dbNameIndex = 0;

    public static DataSource createUniqueDataSource()
    {
        return new SimpleDriverDataSource(new org.h2.Driver(), "jdbc:h2:mem:" + "db_" + dbNameIndex++ + ";DB_CLOSE_DELAY=-1", "root", "");
    }
}
