package com.carbonfive.testutils.easymock;

import org.easymock.*;

public class EasyMockUtils
{

    public static <E> E argAssert(Assertion<E> assertion)
    {
        EasyMock.reportMatcher(new ArgumentAssertion(assertion));
        return null;
    }

}
