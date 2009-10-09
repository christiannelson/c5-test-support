package com.carbonfive.test.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.Test;

import java.util.Properties;

public class PropertiesOverrideProcessorTest
{
    @Test
    public void processShouldOverridePropertiesFromSystemProperties()
    {
        Properties props = new Properties();
        props.setProperty("whatchmacallit.location", "under_door");

        assertThat(props.getProperty("whatchmacallit.location"), is("under_door"));

        System.setProperty("whatchmacallit.location", "in_closet");

        new PropertiesOverrideProcessor(props).process();

        assertThat(props.getProperty("whatchmacallit.location"), is("in_closet"));
    }

    @Test
    public void processShouldResolvePlaceholders()
    {
        Properties props = new Properties();
        props.setProperty("db.host", "localhost");
        props.setProperty("db.name", "db_name");
        props.setProperty("db.url", "jdbc:mysql://${db.host}/${db.name}");

        new PropertiesOverrideProcessor(props).process();

        assertThat(props.getProperty("db.url"), is("jdbc:mysql://localhost/db_name"));
    }
}
