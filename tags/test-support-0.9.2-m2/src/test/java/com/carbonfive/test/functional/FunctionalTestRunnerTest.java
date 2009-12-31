package com.carbonfive.test.functional;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.Properties;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(FunctionalTestRunner.class)
@FunctionalTestConfiguration("classpath:/com/carbonfive/test/functional/functional-tests.properties")
public class FunctionalTestRunnerTest
{
    /* Injected by the test runner. */
    private Properties properties;

    @Test
    public void propertiesShouldBeSetByRunner()
    {
        assertThat(properties, not(nullValue()));
        assertThat(properties.size(), greaterThan(5));
    }

    @Test
    public void applicationShouldBeDeployed() throws Exception
    {
        URL root = new URL(format("http://localhost:%s/", properties.getProperty("appserver.port")));
        String content = IOUtils.toString(root.openStream());
        assertThat(content, containsString("Orange"));
    }
}
