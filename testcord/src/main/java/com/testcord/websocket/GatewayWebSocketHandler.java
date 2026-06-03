package com.testcord.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcord.dto.GatewayEvent;
import com.testcord.dto.UserDTO;
import com.testcord.model.User;
import com.testcord.service.BotTokenService;
import com.testcord.service.GuildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class GatewayWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayWebSocketHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GatewaySessionManager sessionManager;
    private final BotTokenService botTokenService;
    private final GuildService guildService;

    public GatewayWebSocketHandler(GatewaySessionManager sessionManager,
                                    BotTokenService botTokenService,
                                    GuildService guildService) {
        this.sessionManager = sessionManager;
        this.botTokenService = botTokenService;
        this.guildService = guildService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionManager.registerSession(session.getId(), session);
        log.info("Gateway connection opened: {}", session.getId());
        sessionManager.sendToSession(session.getId(), GatewayEvent.hello());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        int op = payload.get("op").asInt();

        switch (op) {
            case GatewayEvent.IDENTIFY -> handleIdentify(session, payload.get("d"));
            case GatewayEvent.HEARTBEAT -> sessionManager.sendToSession(session.getId(), GatewayEvent.heartbeatAck());
            default -> log.warn("Unknown op code {} from session {}", op, session.getId());
        }
    }

    private void handleIdentify(WebSocketSession session, JsonNode data) throws Exception {
        if (data == null || !data.has("token")) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String tokenHeader = data.get("token").asText();
        String token = tokenHeader.startsWith("Bot ") ? tokenHeader.substring(4) : tokenHeader;

        Optional<User> botUser = botTokenService.validateToken(token);
        if (botUser.isEmpty()) {
            log.warn("Invalid token on session {}", session.getId());
            session.close(new CloseStatus(4004, "Authentication failed"));
            return;
        }

        User bot = botUser.get();
        sessionManager.authenticateSession(session.getId(), bot.getId());

        var botGuilds = guildService.findGuildsByMember(bot);
        botGuilds.forEach(g -> sessionManager.subscribeGuild(session.getId(), g.getId()));

        Map<String, Object> readyData = new HashMap<>();
        readyData.put("v", 10);
        readyData.put("user", UserDTO.from(bot));
        readyData.put("guilds", botGuilds.stream().map(g -> Map.of("id", g.getId(), "unavailable", false)).toList());
        readyData.put("session_id", session.getId());

        GatewayEvent ready = GatewayEvent.dispatch("READY", readyData, 0);
        sessionManager.sendToSession(session.getId(), ready);
        log.info("Bot {} identified and READY sent", bot.getUsername());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.removeSession(session.getId());
        log.info("Gateway connection closed: {} status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error on session {}: {}", session.getId(), exception.getMessage());
        sessionManager.removeSession(session.getId());
    }
}
