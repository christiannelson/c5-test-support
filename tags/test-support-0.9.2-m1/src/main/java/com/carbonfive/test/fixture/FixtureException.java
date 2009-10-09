package com.carbonfive.test.fixture;

public class FixtureException extends RuntimeException
{
    public FixtureException()
    {
    }

    public FixtureException(String message)
    {
        super(message);
    }

    public FixtureException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public FixtureException(Throwable cause)
    {
        super(cause);
    }
}