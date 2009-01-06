package com.carbonfive.testutils.spring.dbunit;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNot.*;
import static org.hamcrest.core.IsNull.*;
import static org.hamcrest.text.StringContains.*;
import static org.junit.Assert.*;
import org.junit.*;

public class DataSetTestExecutionListenerTest
{
    @Test
    public void parentClassesCanBeAnnotated() throws NoSuchMethodException
    {
        DataSetTestExecutionListener listener = new DataSetTestExecutionListener();
        DataSetTestExecutionListener.DatasetConfiguration datasetConfiguration =
                listener.determineConfiguration(C.class, C.class.getMethod("testSomething"));

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
