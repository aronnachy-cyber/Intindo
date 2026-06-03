package com.testcord.dto;

import com.testcord.model.Channel;
import lombok.Data;

@Data
public class ChannelDTO {
    private String id;
    private String name;
    private int type;
    private String guildId;
    private String topic;

    public static ChannelDTO from(Channel channel) {
        ChannelDTO dto = new ChannelDTO();
        dto.setId(channel.getId());
        dto.setName(channel.getName());
        dto.setType(channel.getType());
        dto.setTopic(channel.getTopic());
        if (channel.getGuild() != null) {
            dto.setGuildId(channel.getGuild().getId());
        }
        return dto;
    }
}
