package com.carbonfive.testutils.functional;

import org.apache.commons.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.lang.String.format;
import java.net.URL;

@RunWith(FunctionalTestRunner.class)
@FunctionalTestConfiguration(value = "classpath:/com/carbonfive/testutils/functional/functional-tests.properties")
public class FunctionalTestRunnerTest
{
    @Test
    public void applicationShouldBeDeployed() throws Exception
    {
        System.out.println("applicationShouldBeDeployed()");

        URL root = new URL(format("http://localhost:%s/", FunctionalTestProperties.get().getProperty("appserver.port")));
        String content = IOUtils.toString(root.openStream());
        assertThat(content, containsString("Orange"));
    }
}
