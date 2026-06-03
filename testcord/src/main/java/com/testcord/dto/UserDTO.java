package com.testcord.dto;

import com.testcord.model.User;
import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String username;
    private String discriminator;
    private String avatar;
    private boolean bot;

    public static UserDTO from(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDiscriminator(user.getDiscriminator());
        dto.setAvatar(user.getAvatar());
        dto.setBot(user.isBot());
        return dto;
    }
}
