package com.testcord.controller;

import com.testcord.dto.*;
import com.testcord.model.*;
import com.testcord.service.GuildService;
import com.testcord.websocket.GatewaySessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/guilds")
public class GuildController {

    private final GuildService guildService;
    private final GatewaySessionManager gatewaySessionManager;

    public GuildController(GuildService guildService, GatewaySessionManager gatewaySessionManager) {
        this.guildService = guildService;
        this.gatewaySessionManager = gatewaySessionManager;
    }

    @GetMapping("/{guildId}")
    public ResponseEntity<GuildDTO> getGuild(@PathVariable String guildId,
                                              @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return guildService.findById(guildId)
                .map(g -> ResponseEntity.ok(GuildDTO.from(g)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GuildDTO> createGuild(@RequestBody Map<String, String> body,
                                                 @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Guild guild = guildService.createGuild(name, user.getId());
        guildService.addMember(guild, user);
        return ResponseEntity.ok(GuildDTO.from(guild));
    }

    @GetMapping("/{guildId}/members")
    public ResponseEntity<List<GuildMemberDTO>> getMembers(@PathVariable String guildId,
                                                            @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Optional<Guild> guild = guildService.findById(guildId);
        if (guild.isEmpty()) return ResponseEntity.notFound().build();
        List<GuildMember> members = guildService.getMembers(guild.get());
        return ResponseEntity.ok(members.stream().map(GuildMemberDTO::from).toList());
    }

    @DeleteMapping("/{guildId}/members/{userId}")
    public ResponseEntity<Void> kickMember(@PathVariable String guildId,
                                            @PathVariable String userId,
                                            @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Optional<Guild> guild = guildService.findById(guildId);
        if (guild.isEmpty()) return ResponseEntity.notFound().build();

        boolean kicked = guildService.kickMember(guild.get(), userId);
        if (!kicked) return ResponseEntity.notFound().build();

        gatewaySessionManager.broadcastToGuild(guildId, "GUILD_MEMBER_REMOVE",
                Map.of("guild_id", guildId, "user", Map.of("id", userId)));

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{guildId}/members/{userId}")
    public ResponseEntity<Void> addMember(@PathVariable String guildId,
                                           @PathVariable String userId,
                                           @AuthenticationPrincipal User requester) {
        if (requester == null) return ResponseEntity.status(401).build();
        Optional<Guild> guild = guildService.findById(guildId);
        if (guild.isEmpty()) return ResponseEntity.notFound().build();

        return requester.getId().equals(userId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(403).build();
    }

    @PostMapping("/{guildId}/bans/{userId}")
    public ResponseEntity<Void> banUser(@PathVariable String guildId,
                                         @PathVariable String userId,
                                         @RequestBody(required = false) BanRequest banRequest,
                                         @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Optional<Guild> guild = guildService.findById(guildId);
        if (guild.isEmpty()) return ResponseEntity.notFound().build();

        String reason = banRequest != null ? banRequest.getReason() : null;
        boolean banned = guildService.banUser(guild.get(), userId, reason);
        if (!banned) return ResponseEntity.status(409).build();

        gatewaySessionManager.broadcastToGuild(guildId, "GUILD_BAN_ADD",
                Map.of("guild_id", guildId, "user", Map.of("id", userId)));

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{guildId}/bans/{userId}")
    public ResponseEntity<Void> unbanUser(@PathVariable String guildId,
                                           @PathVariable String userId,
                                           @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Optional<Guild> guild = guildService.findById(guildId);
        if (guild.isEmpty()) return ResponseEntity.notFound().build();

        boolean unbanned = guildService.unbanUser(guild.get(), userId);
        if (!unbanned) return ResponseEntity.notFound().build();

        gatewaySessionManager.broadcastToGuild(guildId, "GUILD_BAN_REMOVE",
                Map.of("guild_id", guildId, "user", Map.of("id", userId)));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{guildId}/bans")
    public ResponseEntity<List<Map<String, Object>>> getBans(@PathVariable String guildId,
                                                              @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Optional<Guild> guild = guildService.findById(guildId);
        if (guild.isEmpty()) return ResponseEntity.notFound().build();

        List<Map<String, Object>> bans = guildService.getBans(guild.get()).stream()
                .map(ban -> Map.<String, Object>of(
                        "reason", ban.getReason() != null ? ban.getReason() : "",
                        "user", UserDTO.from(ban.getUser())))
                .toList();
        return ResponseEntity.ok(bans);
    }
}
