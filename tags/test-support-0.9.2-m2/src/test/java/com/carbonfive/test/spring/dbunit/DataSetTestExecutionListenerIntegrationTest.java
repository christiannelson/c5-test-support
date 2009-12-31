package com.carbonfive.test.spring.dbunit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;

@TestExecutionListeners({ DataSetTestExecutionListener.class })
@ContextConfiguration
public class DataSetTestExecutionListenerIntegrationTest extends AbstractJUnit4SpringContextTests
{
    SimpleJdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource)
    {
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    @Test
    public void nonDataSetTestMethodShouldBeIgnored()
    {
    }

    @Test
    @DataSet
    public void dataShouldBeLoadedFromDefaultLocation()
    {
        assertThat(jdbcTemplate.queryForObject("select name from data limit 1", String.class), is("default"));
    }

    @Test
    @DataSet
    public void classpathUrlShouldBeEnabled()
    {
        assertThat(jdbcTemplate.queryForObject("select value from data limit 1", byte[].class), is("chartreuse".getBytes()));
    }

    @Test
    @DataSet("DataSetTestExecutionListenerIntegrationTest2.xml")
    public void dataShouldBeLoadedFromSpecifiedLocation()
    {
        assertThat(jdbcTemplate.queryForObject("select name from data limit 1", String.class), is("2"));
    }

    @Test
    @DataSet(setupOperation = "CLEAN_INSERT")
    public void setupOperationCanBeOverriden()
    {
        // Assert that the setting happened.
    }

    @Test
    @DataSet(teardownOperation = "DELETE_ALL")
    public void teardownOperationCanBeOverriden()
    {
        // Assert that the setting happened.
    }

}


