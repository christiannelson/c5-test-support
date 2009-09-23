package com.carbonfive.testutils.functional;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FunctionalTestProperties
{
    private static Properties properties;

    public static void load(String location) throws IOException
    {
        if (properties != null)
        {
            throw new IllegalStateException("Properties have already been set.");
        }

        Resource resource = new DefaultResourceLoader().getResource(location);
        properties = new Properties();
        InputStream is = resource.getInputStream();
        properties.load(is);
        IOUtils.closeQuietly(is);
    }

    public static Properties get()
    {
        if (properties == null)
        {
            throw new IllegalStateException("Properties have not yet been loaded.");
        }
        return properties;
    }
}
