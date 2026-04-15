package com.example.securelogging.config;

import com.example.securelogging.logging.HeaderSanitizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
public class AppConfig {

    @Bean
    HeaderSanitizer headerSanitizer(LoggingProperties loggingProperties) {
        return new HeaderSanitizer(loggingProperties.getMaskedHeaders());
    }
}
