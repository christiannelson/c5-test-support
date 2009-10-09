package com.carbonfive.test.spring.dbunit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import org.junit.Test;

public class DataSetTestExecutionListenerTest
{
    @Test
    public void parentClassesCanBeAnnotated() throws NoSuchMethodException
    {
        DataSetTestExecutionListener listener = new DataSetTestExecutionListener();
        DataSetTestExecutionListener.DatasetConfiguration datasetConfiguration = listener.determineConfiguration(C.class, C.class.getMethod("testSomething"));

        assertThat(datasetConfiguration, not(nullValue()));
        assertThat(datasetConfiguration.getLocation(), containsString("sample.xml"));
        assertThat(datasetConfiguration.getSetupOperation(), is("INSERT"));
        assertThat(datasetConfiguration.getTeardownOperation(), is("DELETE"));
    }

    @DataSet(value = "sample.xml", setupOperation = "INSERT", teardownOperation = "DELETE")
    static class A
    {
    }

    static class B extends A
    {
    }

    static class C extends B
    {
        public void testSomething()
        {
        }
    }
}
