package com.ssg.jwtredis.service.member;

import com.ssg.jwtredis.dto.MemberDTO;

/**
 * 회원가입 서비스 인터페이스
 */
public interface RegisterService {
    public void register(MemberDTO memberDTO);
}
