package com.testcord.controller;

import com.testcord.dto.*;
import com.testcord.model.*;
import com.testcord.service.ChannelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelDTO> getChannel(@PathVariable String channelId,
                                                   @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return channelService.findById(channelId)
                .map(c -> ResponseEntity.ok(ChannelDTO.from(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{channelId}/messages")
    public ResponseEntity<MessageDTO> sendMessage(@PathVariable String channelId,
                                                    @Valid @RequestBody SendMessageRequest request,
                                                    @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Optional<Channel> channel = channelService.findById(channelId);
        if (channel.isEmpty()) return ResponseEntity.notFound().build();

        Message message = channelService.sendMessage(channel.get(), user, request.getContent());
        return ResponseEntity.ok(MessageDTO.from(message));
    }

    @GetMapping("/{channelId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable String channelId,
                                                          @RequestParam(defaultValue = "50") int limit,
                                                          @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Optional<Channel> channel = channelService.findById(channelId);
        if (channel.isEmpty()) return ResponseEntity.notFound().build();

        List<MessageDTO> messages = channelService.getMessages(channel.get(), limit)
                .stream().map(MessageDTO::from).toList();
        return ResponseEntity.ok(messages);
    }
}
