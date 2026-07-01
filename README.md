# secure-logging-spring-boot-workshop
  * references
    * https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html
    * https://cwe.mitre.org/data/definitions/532.html
    * https://errorprone.info/bugpattern/RestrictedApi
    * https://logback.qos.ch/manual/layouts.html
    * https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/interceptors.html
    * https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties

## preface
  * goals of this workshop
    * introduction to secure logging in Spring Boot applications
    * understanding why secrets should be protected at API, domain and logging layers
    * showing how to sanitize request headers before logging
    * showing how to use Logback masking as defense in depth
    * writing regression tests that prove honeypot secrets do not appear in logs
  * workshop plan
    1. run tests
       * `mvn test`
    2. inspect API boundary types
       * `CreateRequest`
       * `SecretApiInput`
       * verify that raw `apiKey` is not exposed by `toString`
    3. inspect domain secret handling
       * `Secret`
       * verify that raw value is never returned from `toString`
       * verify that `reveal` requires documented intent
    4. inspect request logging
       * `RequestLoggingInterceptor`
       * `HeaderSanitizer`
       * verify that sensitive headers are replaced with `[REDACTED]`
    5. inspect Logback masking
       * `MaskingPatternLayout`
       * `SensitiveDataMasker`
       * verify that accidental raw logs are masked before printing
    6. run application
       * `mvn spring-boot:run`
    7. call endpoint

              curl -i -X POST http://localhost:8080/api/test \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer SECRET_TEST_123" \
                -H "X-API-KEY: API_KEY_HEADER_TEST_789" \
                -d '{"username":"alice","apiKey":"API_KEY_TEST_456"}'

    8. verify logs
       * should contain `[REDACTED]`
       * should not contain `SECRET_TEST_123`
       * should not contain `API_KEY_TEST_456`
       * should not contain `API_KEY_HEADER_TEST_789`

## secure logging
  * sensitive data in logs is a security problem
    * logs are often copied to external systems
      * observability platforms
      * support tools
      * incident archives
    * logs usually have different access rules than production databases
    * once a secret is written to logs, rotation is usually required
  * examples of values that should not be logged
    * passwords
    * api keys
    * bearer tokens
    * cookies
    * session identifiers
    * authorization headers
  * secure logging should be implemented as layers
    * avoid logging request and response bodies by default
    * make sensitive API fields safe to render
    * make domain secret objects safe to render
    * sanitize headers before logging
    * mask known patterns in final log output
    * test that known honeypot values never appear in logs

## api boundary
  * `SecretApiInput`
    * wraps incoming secret value at the API edge
    * uses delegating JSON creator
      * payload can use a simple string value
    * overrides `toString`
      * returns `[REDACTED]`
  * `CreateRequest`
    * contains username and api key
    * generated record `toString` is safe because `SecretApiInput.toString` is safe
  * endpoint intentionally logs request object
    * purpose: prove that DTO rendering does not leak raw api key

## domain secret
  * `Secret`
    * wraps raw secret material
    * overrides `toString`
      * returns `[REDACTED]`
    * exposes raw value only through `reveal`
  * `reveal`
    * is marked with Error Prone `@RestrictedApi`
    * allowed only for code annotated with `@SecretAccessPurpose`
    * makes raw secret access explicit at call site
  * this does not replace review or testing
    * it makes accidental usage harder
    * it documents intentional usage

## request logging
  * `RequestLoggingInterceptor`
    * logs method
    * logs path
    * logs status
    * logs sanitized headers
  * it does not log request or response body
    * bodies often contain credentials, tokens and personal data
  * `HeaderSanitizer`
    * receives names of headers that must be masked
    * compares names case-insensitively
    * replaces configured header values with `[REDACTED]`
  * default masked headers
    * `Authorization`
    * `Cookie`
    * `Set-Cookie`
    * `X-API-KEY`
  * configuration

            app:
              logging:
                masked-headers:
                  - Authorization
                  - Cookie
                  - Set-Cookie
                  - X-API-KEY

## logback masking
  * `MaskingPatternLayout`
    * extends Logback `PatternLayout`
    * delegates normal formatting to Logback
    * applies `SensitiveDataMasker` before log line is emitted
  * `SensitiveDataMasker`
    * masks key-value patterns
      * `apiKey=...`
      * `token=...`
      * `password=...`
    * masks JSON patterns
      * `"apiKey":"..."`
      * `"token":"..."`
      * `"password":"..."`
    * masks selected header patterns
      * `Authorization`
      * `Cookie`
      * `Set-Cookie`
      * `X-API-KEY`
  * masking is defense in depth
    * it should not be the first protection layer
    * it protects against accidental logging paths

## tests
  * `SecureLoggingIntegrationTest`
    * captures Spring Boot logs
    * sends requests with honeypot secret values
    * verifies that raw values do not appear in output
    * verifies that `[REDACTED]` appears instead
  * important test cases
    * secret from `Authorization` header must be masked
    * secret from JSON `apiKey` must be masked
    * accidental raw `Secret.reveal()` logging must still be masked
  * example assertion

            assertThat(logs).doesNotContain(secret);
            assertThat(logs).doesNotContain(apiKey);
            assertThat(logs).contains("[REDACTED]");

## exercises
  1. break one layer intentionally
     * remove `SecretApiInput.toString`
     * run tests
     * restore it after observing failure

## summary
  * secure logging is not one mechanism
  * safe API and domain types prevent common accidental leaks
  * sanitized request logging avoids header leaks
  * Logback masking catches remaining accidental output
  * regression tests should contain realistic honeypot values
