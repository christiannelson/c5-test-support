package com.carbonfive.testutils.fixture;

import java.sql.*;

public interface Fixture
{
    void load(Connection connection) throws FixtureException;
}
