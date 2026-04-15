package com.example.securelogging.test;

import com.example.securelogging.domain.Secret;
import com.example.securelogging.domain.SecretAccessPurpose;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class SecureLoggingIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(SecureLoggingIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    void honeypotSecretsMustNotAppearInLogs(CapturedOutput output) throws Exception {
        String secret = "SECRET_TEST_123";
        String apiKey = "API_KEY_TEST_456";
        String payload = """
                {
                  "username": "alice",
                  "apiKey": "%s"
                }
                """.formatted(apiKey);

        mockMvc.perform(post("/api/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", "Bearer " + secret))
                .andExpect(status().isCreated());

        String logs = output.getOut();
        assertThat(logs).doesNotContain(secret);
        assertThat(logs).doesNotContain(apiKey);
        assertThat(logs).contains("[REDACTED]");
    }

    @Test
    void authorizationHeaderMustBeMasked(CapturedOutput output) throws Exception {
        String auth = "Bearer AUTH_SECRET_VALUE";
        String payload = """
                {
                  "username": "bob",
                  "apiKey": "ANY_VALUE"
                }
                """;

        mockMvc.perform(post("/api/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", auth))
                .andExpect(status().isCreated());

        String logs = output.getOut();
        assertThat(logs).doesNotContain(auth);
        assertThat(logs).contains("Authorization=[REDACTED]");
    }

    @Test
    void regressionAccidentalRevealLoggingStillMasked(CapturedOutput output) {
        Secret secret = Secret.of("API_KEY_TEST_456");
        @SecretAccessPurpose("Regression test intentionally exercises raw reveal logging path")
        var revealed = secret.reveal();
        log.info("apiKey={}", revealed);

        String logs = output.getOut();
        assertThat(logs).doesNotContain("API_KEY_TEST_456");
        assertThat(logs).contains("apiKey=[REDACTED]");
    }
}
