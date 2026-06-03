package com.testcord.dto;

import com.testcord.model.Guild;
import lombok.Data;

@Data
public class GuildDTO {
    private String id;
    private String name;
    private String icon;
    private String ownerId;
    private int memberCount;

    public static GuildDTO from(Guild guild) {
        GuildDTO dto = new GuildDTO();
        dto.setId(guild.getId());
        dto.setName(guild.getName());
        dto.setIcon(guild.getIcon());
        dto.setOwnerId(guild.getOwnerId());
        if (guild.getMembers() != null) {
            dto.setMemberCount(guild.getMembers().size());
        }
        return dto;
    }
}
