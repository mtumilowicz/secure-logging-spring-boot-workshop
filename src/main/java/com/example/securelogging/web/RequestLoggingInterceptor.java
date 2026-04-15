package com.example.securelogging.web;

import com.example.securelogging.logging.HeaderSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private final HeaderSanitizer headerSanitizer;

    public RequestLoggingInterceptor(HeaderSanitizer headerSanitizer) {
        this.headerSanitizer = headerSanitizer;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception ex
    ) {
        Map<String, String> safeHeaders = headerSanitizer.sanitize(request);
        int status = response != null ? response.getStatus() : 500;

        // No request/response body logging by design.
        log.info(
                "http method={} path={} status={} headers={}",
                request.getMethod(),
                request.getRequestURI(),
                status,
                safeHeaders
        );
    }
}
