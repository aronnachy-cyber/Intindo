package com.testcord.dto;

import com.testcord.model.Message;
import lombok.Data;
import java.time.Instant;

@Data
public class MessageDTO {
    private String id;
    private String channelId;
    private UserDTO author;
    private String content;
    private Instant timestamp;
    private Instant editedTimestamp;
    private String guildId;

    public static MessageDTO from(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setChannelId(message.getChannel().getId());
        dto.setAuthor(UserDTO.from(message.getAuthor()));
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setEditedTimestamp(message.getEditedTimestamp());
        dto.setGuildId(message.getGuildId());
        return dto;
    }
}
