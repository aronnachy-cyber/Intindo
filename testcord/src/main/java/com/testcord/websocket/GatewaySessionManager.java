package com.testcord.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcord.dto.GatewayEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GatewaySessionManager {

    private static final Logger log = LoggerFactory.getLogger(GatewaySessionManager.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToBot = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> botToGuilds = new ConcurrentHashMap<>();
    private final AtomicInteger globalSeq = new AtomicInteger(0);

    public void registerSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
    }

    public void authenticateSession(String sessionId, String botUserId) {
        sessionToBot.put(sessionId, botUserId);
        log.info("Bot {} authenticated on session {}", botUserId, sessionId);
    }

    public void subscribeGuild(String sessionId, String guildId) {
        String botId = sessionToBot.get(sessionId);
        if (botId != null) {
            botToGuilds.computeIfAbsent(botId, k -> ConcurrentHashMap.newKeySet()).add(guildId);
        }
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        sessionToBot.remove(sessionId);
    }

    public void sendToSession(String sessionId, GatewayEvent event) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(event);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                log.error("Failed to send to session {}: {}", sessionId, e.getMessage());
            }
        }
    }

    public void broadcastToGuild(String guildId, String eventType, Object data) {
        if (guildId == null) return;
        int seq = globalSeq.incrementAndGet();
        GatewayEvent event = GatewayEvent.dispatch(eventType, data, seq);

        sessions.forEach((sessionId, session) -> {
            String botId = sessionToBot.get(sessionId);
            if (botId != null) {
                Set<String> guilds = botToGuilds.getOrDefault(botId, Collections.emptySet());
                if (guilds.contains(guildId)) {
                    sendToSession(sessionId, event);
                }
            }
        });
    }

    public void broadcastToAll(String eventType, Object data) {
        int seq = globalSeq.incrementAndGet();
        GatewayEvent event = GatewayEvent.dispatch(eventType, data, seq);
        sessions.keySet().forEach(sessionId -> {
            if (sessionToBot.containsKey(sessionId)) {
                sendToSession(sessionId, event);
            }
        });
    }

    public boolean isAuthenticated(String sessionId) {
        return sessionToBot.containsKey(sessionId);
    }

    public String getBotForSession(String sessionId) {
        return sessionToBot.get(sessionId);
    }
}
