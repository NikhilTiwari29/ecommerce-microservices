package com.nikhil.microservices.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GatewayFallbackController {

    @GetMapping("/_fallback")
    public ResponseEntity<Map<String, Object>> fallback() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 503);
        body.put("message", "Downstream service is unavailable");
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(body);
    }
}
