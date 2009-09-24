package com.carbonfive.test.functional;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesOverrideProcessor
{
    private final Properties properties;

    public PropertiesOverrideProcessor(Properties properties)
    {
        this.properties = properties;
    }

    public void process()
    {
        // Check for system property overrides.
        for (Object key : properties.keySet())
        {
            properties.put(key, System.getProperty(key.toString(), properties.getProperty(key.toString())));
        }

        Pattern placeholderPattern = Pattern.compile(".*(\\$\\{(.+?)\\}).*");

        // Check for embedded placeholders.
        for (Object key : properties.keySet())
        {
            String value = properties.getProperty(key.toString());

            Matcher matcher = placeholderPattern.matcher(value);

            while (matcher.find())
            {
                String placeholderName = matcher.group(2);
                value = value.substring(0, matcher.start(1)) + properties.getProperty(placeholderName) + value.substring(matcher.end(1), value.length());
                matcher = placeholderPattern.matcher(value);
            }

            properties.setProperty(key.toString(), value);
        }
    }
}
