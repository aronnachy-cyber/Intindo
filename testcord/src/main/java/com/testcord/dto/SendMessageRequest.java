package com.testcord.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotBlank(message = "Content must not be blank")
    private String content;
}
