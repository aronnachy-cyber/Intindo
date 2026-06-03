package com.testcord.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "oauth_access_tokens")
@Data
@NoArgsConstructor
public class OAuthAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, unique = true)
    private String refreshToken;

    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;

    @Column(name = "client_id", nullable = false, length = 20)
    private String clientId;

    @Column(nullable = false)
    private String scope;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public OAuthAccessToken(String accessToken, String refreshToken, String userId, String clientId, String scope) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.clientId = clientId;
        this.scope = scope;
        this.expiresAt = Instant.now().plusSeconds(604800);
    }
}
