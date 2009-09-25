package com.carbonfive.test.functional;

import java.util.Properties;

public interface ApplicationServerManager
{
    void initialize(Properties properties);

    void start();
    
    void stop();
}
