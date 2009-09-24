package com.carbonfive.testutils.functional;

import java.util.Properties;

public interface ApplicationServerManager
{
    void initialize(Properties properties);

    boolean isRunning();

    void start();
    
    void stop();
}
