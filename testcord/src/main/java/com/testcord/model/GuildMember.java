package com.testcord.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "guild_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"guild_id", "user_id"})
})
@Data
@NoArgsConstructor
public class GuildMember {

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
    private String nick;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    public GuildMember(Guild guild, User user) {
        this.guild = guild;
        this.user = user;
    }
}
