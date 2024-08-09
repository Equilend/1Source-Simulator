package com.equilend.simulator.utils;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    public static final String PROPERTIES_FILE = "application.properties";

    public static Properties loadProperties(String... args) {
        Properties properties = loadDefaultProperties();
        Properties argumentProperties = parseArguments(args);
        Properties configFileProperties = new Properties();
        String configFileName = argumentProperties.containsKey(PROPERTIES_FILE)? argumentProperties.getProperty(PROPERTIES_FILE) : System.getProperty(PROPERTIES_FILE);
        if (configFileName!=null && !configFileName.isEmpty()) {
            configFileProperties = parsePropertiesFile(configFileName);
        }
        properties.putAll(configFileProperties);
        properties.putAll(argumentProperties);
        return properties;
    }

    private static Properties loadDefaultProperties() {
        Properties properties = new Properties();
        InputStream propertiesStream = PropertiesUtil.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        try {
            properties.load(propertiesStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    public static Properties parseArguments(String... args) {
        Properties properties = new Properties();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String optionText = arg.substring(2);
                String optionName;
                String optionValue = null;
                int indexOfEqualsSign = optionText.indexOf('=');
                if (indexOfEqualsSign > -1) {
                    optionName = optionText.substring(0, indexOfEqualsSign);
                    optionValue = optionText.substring(indexOfEqualsSign + 1);
                } else {
                    optionName = optionText;
                }
                if (optionName.isEmpty()) {
                    throw new IllegalArgumentException("Invalid argument syntax: " + arg);
                }
                properties.setProperty(optionName, optionValue);
            }
        }
        return properties;
    }

    public static Properties parsePropertiesFile(String fileName) {
        Properties properties = new Properties();
        try {
            FileReader in = new FileReader(fileName);
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

}
