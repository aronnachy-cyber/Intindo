package com.testcord.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "oauth_clients")
@Data
@NoArgsConstructor
public class OAuthClient {

    @Id
    @Column(length = 20)
    private String clientId;

    @Column(nullable = false)
    private String clientSecret;

    @Column(nullable = false)
    private String name;

    @Column(name = "redirect_uri", nullable = false)
    private String redirectUri;

    @Column(name = "owner_user_id", length = 20)
    private String ownerUserId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public OAuthClient(String clientId, String clientSecret, String name, String redirectUri, String ownerUserId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.name = name;
        this.redirectUri = redirectUri;
        this.ownerUserId = ownerUserId;
    }
}
