package com.testcord.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "guilds")
@Data
@NoArgsConstructor
public class Guild {

    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column
    private String icon;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GuildMember> members;

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Channel> channels;

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ban> bans;

    public Guild(String id, String name, String ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
    }
}
