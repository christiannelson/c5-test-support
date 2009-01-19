package com.carbonfive.testutils.easymock;

import org.easymock.*;

public class ArgumentAssertion implements IArgumentMatcher
{

    private Assertion assertion;
    private Error assertionError;

    public ArgumentAssertion(Assertion assertion)
    {
        this.assertion = assertion;
    }

    @SuppressWarnings("unchecked")
    public boolean matches(Object actual)
    {
        try
        {
            assertion.check(actual);
            return true;
        }
        catch (Error e)
        {
            assertionError = e;
            return false;
        }
        catch (Exception e)
        {
            assertionError = new Error(e);
            return false;
        }
    }

    public void appendTo(StringBuffer buffer)
    {
        buffer.append("argumentAssertion(exception ").append(assertionError).append(")");
    }
}