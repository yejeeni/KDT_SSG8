package com.ssg.jwtredis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 토큰을 클라이언트에게 전송할 때, json으로 자동으로 변환시키기 위한 DTO
 */
@Getter @Setter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;

    private long expireSecond;
}
