package com.testcord.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateBotResponse {
    private UserDTO bot;
    private String token;
}
