package com.example.securelogging.web;

public record CreateRequest(
        String username,
        SecretApiInput apiKey
) {
}
