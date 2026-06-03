package com.testcord.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBotRequest {
    @NotBlank
    private String username;
}
