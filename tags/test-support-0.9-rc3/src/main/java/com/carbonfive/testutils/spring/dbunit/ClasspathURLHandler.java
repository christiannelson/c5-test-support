package com.carbonfive.testutils.spring.dbunit;

import org.slf4j.*;
import org.springframework.core.io.*;

import java.io.*;
import java.net.*;

public class ClasspathURLHandler extends URLStreamHandler
{
    private static final Logger log = LoggerFactory.getLogger(ClasspathURLHandler.class);

    public static void register()
    {
        try
        {
            URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory()
            {
                public URLStreamHandler createURLStreamHandler(String protocol)
                {
                    return "classpath".equals(protocol) ? new ClasspathURLHandler() : null;
                }
            });
        }
        catch (Exception e)
        {
            log.error("Failed to set URL.URLStreamHandlerFactory.  Factory is probably already set.");
        }
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException
    {
        String path = u.getHost() + "/" + u.getPath();
        ClassPathResource resource = new ClassPathResource(path);
        URL url = resource.getURL();
        return url == null ? null : url.openConnection();
    }
}