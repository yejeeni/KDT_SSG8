package com.ssg.jwtredis.service.member;

import com.ssg.jwtredis.domain.Member;
import com.ssg.jwtredis.dto.MemberDTO;

public interface MemberService {
    public Member register(MemberDTO memberDTO);
}
