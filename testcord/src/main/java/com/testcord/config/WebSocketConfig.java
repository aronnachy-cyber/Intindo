package com.testcord.config;

import com.testcord.websocket.GatewayWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GatewayWebSocketHandler gatewayHandler;

    public WebSocketConfig(GatewayWebSocketHandler gatewayHandler) {
        this.gatewayHandler = gatewayHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gatewayHandler, "/gateway")
                .setAllowedOrigins("*");
    }
}
