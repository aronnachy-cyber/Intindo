package com.testcord.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
public class Message {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "guild_id", length = 20)
    private String guildId;

    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    @Column(name = "edited_timestamp")
    private Instant editedTimestamp;

    public Message(String id, Channel channel, User author, String content) {
        this.id = id;
        this.channel = channel;
        this.author = author;
        this.content = content;
        if (channel.getGuild() != null) {
            this.guildId = channel.getGuild().getId();
        }
    }
}
