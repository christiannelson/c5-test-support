package com.carbonfive.testutils.spring.dbunit;

import org.springframework.jdbc.core.simple.*;

import javax.sql.*;

public class DatabaseInitializer
{
    private SimpleJdbcTemplate jdbcTemplate;
    private String sql;

    public DatabaseInitializer(DataSource dataSource)
    {
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    public String getSql()
    {
        return sql;
    }

    public void setSql(String sql)
    {
        this.sql = sql;
    }

    public void initialize()
    {
        jdbcTemplate.update(getSql());
    }
}
