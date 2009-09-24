package com.carbonfive.test.functional;

public class DatabaseFixtureException extends RuntimeException
{
    public DatabaseFixtureException(String messsage, Exception e)
    {
        super(messsage, e);
    }
}
