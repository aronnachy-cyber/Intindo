package com.testcord.controller;

import com.testcord.dto.*;
import com.testcord.model.User;
import com.testcord.service.BotTokenService;
import com.testcord.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final BotTokenService botTokenService;

    public AuthController(UserService userService, BotTokenService botTokenService) {
        this.userService = userService;
        this.botTokenService = botTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            User user = userService.register(body.get("username"), body.get("password"));
            return ResponseEntity.ok(UserDTO.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        Optional<User> user = userService.authenticate(body.get("username"), body.get("password"));
        if (user.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }
        String oauthCode = "user_session_" + user.get().getId();
        return ResponseEntity.ok(Map.of("user", UserDTO.from(user.get()), "user_id", user.get().getId()));
    }

    @PostMapping("/bots")
    public ResponseEntity<CreateBotResponse> createBot(@Valid @RequestBody CreateBotRequest request,
                                                        @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        String token = botTokenService.createBot(request.getUsername());
        return botTokenService.validateToken(token)
                .map(bot -> ResponseEntity.ok(new CreateBotResponse(UserDTO.from(bot), token)))
                .orElse(ResponseEntity.internalServerError().build());
    }
}
