package com.carbonfive.testutils.easymock;

import static com.carbonfive.testutils.easymock.EasyMockUtils.*;
import org.easymock.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import org.junit.*;

public class ArgumentAssertionTest
{

    public interface TestInterface
    {
        void testMethod(String arg);
    }

    @Test
    public void captureTest()
    {
        // Example of using the new capture functionality added in easymock 2.4.
        TestInterface mock = createMock(TestInterface.class);

        Capture<String> c = new Capture<String>();
        mock.testMethod(capture(c));
        replay(mock);

        mock.testMethod("xian");

        assertThat(c.getValue(), is("xian"));
    }

    @Test
    public void testArgumentAssertion()
    {
        TestInterface mock = createMock(TestInterface.class);

        //record expected behavior
        mock.testMethod(argAssert(new Assertion<String>()
        {
            public void check(String argument)
            {
                //do nothing - should pass
            }
        }));

        //should get no errors
        replay(mock);
        mock.testMethod("test");
        verify(mock);

        reset(mock);

        //record expected behavior
        mock.testMethod(argAssert(new Assertion<String>()
        {
            public void check(String argument)
            {
                fail("explicitely fail assertion");
            }
        }));

        replay(mock);

        boolean testSucceeded = false;

        try
        {
            mock.testMethod("test");
            verify(mock);
            testSucceeded = true;
        }
        catch (AssertionError e)
        {
            //should get this error
        }

        if (testSucceeded)
        {
            fail("should have gotten a failure");
        }
    }
}
