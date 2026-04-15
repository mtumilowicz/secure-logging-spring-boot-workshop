package com.example.securelogging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "app.logging")
public class LoggingProperties {
    private Set<String> maskedHeaders = new LinkedHashSet<>(
            Set.of("Authorization", "Cookie", "Set-Cookie", "X-API-KEY")
    );

    public Set<String> getMaskedHeaders() {
        return maskedHeaders;
    }

    public void setMaskedHeaders(Set<String> maskedHeaders) {
        if (maskedHeaders == null || maskedHeaders.isEmpty()) {
            return;
        }
        this.maskedHeaders = maskedHeaders.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
