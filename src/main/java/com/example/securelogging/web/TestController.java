package com.example.securelogging.web;

import com.example.securelogging.domain.Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @PostMapping("/test")
    public ResponseEntity<CreateResponse> create(@RequestBody CreateRequest request) {
        Secret domainSecret = request.apiKey().toDomain();
        // Intentionally log full DTO and domain secret: both stay redacted.
        log.info("request={} domainSecret={}", request, domainSecret);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateResponse("created"));
    }

    public record CreateResponse(String status) {
    }
}
