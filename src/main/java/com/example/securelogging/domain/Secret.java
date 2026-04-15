package com.example.securelogging.domain;

import com.google.errorprone.annotations.RestrictedApi;

/**
 * Security value object: raw value is never exposed through toString.
 */
public record Secret(String value) {

    public static Secret of(String value) {
        return new Secret(value);
    }

    /**
     * Accessing raw secret material is intentionally restricted.
     * Call sites must declare an explicit purpose via @SecretAccessPurpose.
     */
    @RestrictedApi(
            explanation = "Do not reveal secrets unless there is a documented business/security reason.",
            link = "https://errorprone.info/bugpattern/RestrictedApi",
            allowlistAnnotations = SecretAccessPurpose.class
    )
    public String reveal() {
        return value;
    }

    @Override
    public String toString() {
        return "[REDACTED]";
    }
}
