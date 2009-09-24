package com.carbonfive.test.easymock;

import org.easymock.EasyMock;

public class EasyMockUtils
{

    public static <E> E argAssert(Assertion<E> assertion)
    {
        EasyMock.reportMatcher(new ArgumentAssertion(assertion));
        return null;
    }

}
