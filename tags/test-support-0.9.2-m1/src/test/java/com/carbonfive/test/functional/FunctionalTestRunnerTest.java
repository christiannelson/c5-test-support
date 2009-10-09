package com.carbonfive.test.functional;

import org.apache.commons.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.lang.String.format;
import java.net.URL;
import java.util.Properties;

@RunWith(FunctionalTestRunner.class)
@FunctionalTestConfiguration("classpath:/com/carbonfive/test/functional/functional-tests.properties")
public class FunctionalTestRunnerTest
{
    /* Injected by the test runner. */
    private Properties properties;

    @Test
    public void propertiesShouldBeSetByRunner()
    {
        System.out.println("propertiesShouldBeSetByRunner()");

        assertThat(properties, not(nullValue()));
        assertThat(properties.size(), greaterThan(5));
    }

    @Test
    public void applicationShouldBeDeployed() throws Exception
    {
        System.out.println("applicationShouldBeDeployed()");

        URL root = new URL(format("http://localhost:%s/", properties.getProperty("appserver.port")));
        String content = IOUtils.toString(root.openStream());
        assertThat(content, containsString("Orange"));
    }
}
