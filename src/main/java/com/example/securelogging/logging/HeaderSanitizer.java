package com.example.securelogging.logging;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Central place for safe header rendering.
 */
public class HeaderSanitizer {
    private static final String REDACTED = "[REDACTED]";
    private final Set<String> maskedHeaderNamesLowerCase;

    public HeaderSanitizer(Set<String> maskedHeaderNames) {
        Set<String> safeInput = maskedHeaderNames == null ? Collections.emptySet() : maskedHeaderNames;
        this.maskedHeaderNamesLowerCase = safeInput.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    public Map<String, String> sanitize(HttpServletRequest request) {
        Map<String, String> sanitized = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            List<String> values = Collections.list(request.getHeaders(name));
            String joined = String.join(",", values);
            sanitized.put(name, sanitize(name, joined));
        }
        return sanitized;
    }

    public String sanitize(String headerName, String value) {
        if (headerName == null) {
            return value;
        }
        if (maskedHeaderNamesLowerCase.contains(headerName.toLowerCase(Locale.ROOT))) {
            return REDACTED;
        }
        return value;
    }
}
