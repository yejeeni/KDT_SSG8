package com.ssg.jwtredis.service.member;

import com.ssg.jwtredis.dto.MemberDTO;
import com.ssg.jwtredis.service.email.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 회원가입 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final EmailVerificationService emailVerificationService;

    /**
     * 회원가입
     */
    @Override
    public void register(MemberDTO memberDTO) {
        // code 추가
        memberDTO.setCode(emailVerificationService.generateEmailValidCode());

        // 임시 회원 정보를 redis에 등록
        emailVerificationService.savePending(memberDTO);
    }

}
