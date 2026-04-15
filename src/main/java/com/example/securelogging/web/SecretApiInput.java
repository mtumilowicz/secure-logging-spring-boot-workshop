package com.example.securelogging.web;

import com.example.securelogging.domain.Secret;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * API boundary type: never exposes raw value directly.
 */
public record SecretApiInput(String value) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public SecretApiInput {
    }

    public Secret toDomain() {
        return Secret.of(value);
    }

    @Override
    public String toString() {
        return "[REDACTED]";
    }
}
