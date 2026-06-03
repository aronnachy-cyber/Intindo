package com.testcord.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class GatewayController {

    @GetMapping("/gateway")
    public ResponseEntity<Map<String, Object>> getGateway() {
        return ResponseEntity.ok(Map.of(
                "url", "ws://localhost:5000/gateway",
                "shards", 1,
                "session_start_limit", Map.of(
                        "total", 1000,
                        "remaining", 999,
                        "reset_after", 14400000,
                        "max_concurrency", 1
                )
        ));
    }

    @GetMapping("/gateway/bot")
    public ResponseEntity<Map<String, Object>> getGatewayBot() {
        return getGateway();
    }
}
