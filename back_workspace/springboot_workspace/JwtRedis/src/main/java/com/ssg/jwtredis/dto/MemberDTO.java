package com.ssg.jwtredis.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 회원가입에 사용하는 Member DTO
 */
@Getter @Setter
public class MemberDTO {
    private int memberId;

    private String loginId;

    private String password;

    private String name;

    private String email;

    private String code;
    
    private String deviceId; // 사용 중인 디바이스의 고유값
}
