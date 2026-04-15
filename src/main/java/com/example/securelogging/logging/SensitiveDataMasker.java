package com.example.securelogging.logging;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Defense-in-depth masking for accidental secret logging.
 */
public final class SensitiveDataMasker {
    private static final String REDACTED = "[REDACTED]";

    private static final List<MaskRule> RULES = List.of(
            new MaskRule(
                    Pattern.compile("(?i)(\\b(?:apiKey|token|password)\\b\\s*=\\s*)([^,;\\s\\]}]+)"),
                    "$1" + REDACTED
            ),
            new MaskRule(
                    Pattern.compile("(?i)(\"(?:apiKey|token|password)\"\\s*:\\s*\")([^\"]+)(\")"),
                    "$1" + REDACTED + "$3"
            ),
            new MaskRule(
                    Pattern.compile("(?i)(\\b(?:Authorization|Cookie|Set-Cookie|X-API-KEY)\\b\\s*=\\s*\\[)([^\\]]+)(\\])"),
                    "$1" + REDACTED + "$3"
            ),
            new MaskRule(
                    Pattern.compile("(?i)(\\b(?:Authorization|Cookie|Set-Cookie|X-API-KEY)\\b\\s*[:=]\\s*)([^,;\\n]+)"),
                    "$1" + REDACTED
            )
    );

    private SensitiveDataMasker() {
    }

    public static String mask(String input) {
        String masked = input;
        for (MaskRule rule : RULES) {
            masked = rule.apply(masked);
        }
        return masked;
    }

    private record MaskRule(Pattern pattern, String replacement) {
        private String apply(String value) {
            return pattern.matcher(value).replaceAll(replacement);
        }
    }
}
