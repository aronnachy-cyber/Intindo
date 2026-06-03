package com.testcord.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "bot_tokens")
@Data
@NoArgsConstructor
public class BotToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_user_id", nullable = false)
    private User botUser;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public BotToken(String token, User botUser) {
        this.token = token;
        this.botUser = botUser;
    }
}
