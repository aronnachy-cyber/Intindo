package com.testcord.dto;

import lombok.Data;

@Data
public class BanRequest {
    private String reason;
    private int deleteMessageSeconds = 0;
}
