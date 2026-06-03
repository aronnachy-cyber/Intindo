package com.testcord.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "channels")
@Data
@NoArgsConstructor
public class Channel {

    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column
    private int type = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id")
    private Guild guild;

    @Column
    private String topic;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;

    public Channel(String id, String name, Guild guild) {
        this.id = id;
        this.name = name;
        this.guild = guild;
    }
}
