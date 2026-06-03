package com.testcord.controller;

import com.testcord.dto.*;
import com.testcord.model.Guild;
import com.testcord.model.User;
import com.testcord.service.GuildService;
import com.testcord.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final GuildService guildService;

    public UserController(UserService userService, GuildService guildService) {
        this.userService = userService;
        this.guildService = guildService;
    }

    @GetMapping("/@me")
    public ResponseEntity<UserDTO> getMe(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(UserDTO.from(user));
    }

    @GetMapping("/@me/guilds")
    public ResponseEntity<List<GuildDTO>> getMyGuilds(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        List<Guild> guilds = guildService.findGuildsByMember(user);
        return ResponseEntity.ok(guilds.stream().map(GuildDTO::from).toList());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String userId) {
        return userService.findById(userId)
                .map(u -> ResponseEntity.ok(UserDTO.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/@me/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "username and password required"));
            }
            User user = userService.register(username, password);
            return ResponseEntity.ok(UserDTO.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
