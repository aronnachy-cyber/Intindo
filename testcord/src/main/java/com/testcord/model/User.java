package com.testcord.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column
    private String discriminator = "0000";

    @Column
    private String avatar;

    @Column(nullable = false)
    private boolean bot = false;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<GuildMember> guilds;

    public User(String id, String username, boolean bot) {
        this.id = id;
        this.username = username;
        this.bot = bot;
    }
}
