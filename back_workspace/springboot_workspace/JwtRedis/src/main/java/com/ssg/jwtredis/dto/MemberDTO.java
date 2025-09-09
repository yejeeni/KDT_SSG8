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
}
