package com.ssg.jwtredis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LogoutRequest {
    private String accessToken;

    private String deviceId;
}
