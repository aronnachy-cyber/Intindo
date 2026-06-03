package com.testcord.security;

import com.testcord.model.User;
import com.testcord.service.BotTokenService;
import com.testcord.service.OAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class BotTokenAuthFilter extends OncePerRequestFilter {

    private final BotTokenService botTokenService;
    private final OAuthService oAuthService;

    public BotTokenAuthFilter(BotTokenService botTokenService, OAuthService oAuthService) {
        this.botTokenService = botTokenService;
        this.oAuthService = oAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null) {
            Optional<User> user = Optional.empty();

            if (authHeader.startsWith("Bot ")) {
                String token = authHeader.substring(4).trim();
                user = botTokenService.validateToken(token);
            } else if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                user = oAuthService.validateAccessToken(token);
            }

            if (user.isPresent()) {
                var auth = new UsernamePasswordAuthenticationToken(
                        user.get(),
                        null,
                        List.of(new SimpleGrantedAuthority(user.get().isBot() ? "ROLE_BOT" : "ROLE_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
