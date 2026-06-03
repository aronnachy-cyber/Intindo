package com.testcord.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "bans", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"guild_id", "user_id"})
})
@Data
@NoArgsConstructor
public class Ban {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id", nullable = false)
    private Guild guild;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Ban(Guild guild, User user, String reason) {
        this.guild = guild;
        this.user = user;
        this.reason = reason;
    }
}
