package com.ucmmaster.kafka.streams;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    static @NonNull Properties getProperties() throws IOException {
        Properties props = new Properties();
        String config = "streams.properties";
        try (InputStream fis = KStreamApp.class.getClassLoader().getResourceAsStream(config)) {
            props.load(fis);
        }
        return props;
    }
}
