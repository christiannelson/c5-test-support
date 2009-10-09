package com.carbonfive.test.functional;

import java.lang.reflect.Method;
import java.util.Properties;

public interface DatabaseFixtureLoader
{
    void initialize(Properties properties) throws ClassNotFoundException;

    void load(Class<?> testClass, Method testMethod) throws DatabaseFixtureException;
}
