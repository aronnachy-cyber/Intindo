package com.testcord.dto;

import com.testcord.model.GuildMember;
import lombok.Data;
import java.time.Instant;

@Data
public class GuildMemberDTO {
    private UserDTO user;
    private String nick;
    private Instant joinedAt;

    public static GuildMemberDTO from(GuildMember member) {
        GuildMemberDTO dto = new GuildMemberDTO();
        dto.setUser(UserDTO.from(member.getUser()));
        dto.setNick(member.getNick());
        dto.setJoinedAt(member.getJoinedAt());
        return dto;
    }
}
