package com.carbonfive.test.fixture;

import java.sql.Connection;

public interface Fixture
{
    void load(Connection connection) throws FixtureException;
}
