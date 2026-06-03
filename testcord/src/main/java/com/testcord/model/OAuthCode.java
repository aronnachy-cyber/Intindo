package com.testcord.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "oauth_codes")
@Data
@NoArgsConstructor
public class OAuthCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;

    @Column(name = "client_id", nullable = false, length = 20)
    private String clientId;

    @Column(nullable = false)
    private String scope;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public OAuthCode(String code, String userId, String clientId, String scope) {
        this.code = code;
        this.userId = userId;
        this.clientId = clientId;
        this.scope = scope;
        this.expiresAt = Instant.now().plusSeconds(300);
    }
}
