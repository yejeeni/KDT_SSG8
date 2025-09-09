package com.ssg.jwtredis.controller;

import com.ssg.jwtredis.dto.MemberDTO;
import com.ssg.jwtredis.security.SecurityConfig;
import com.ssg.jwtredis.service.member.MemberService;
import com.ssg.jwtredis.service.member.RegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final RegisterService registerService;
    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     * @param memberDTO
     * @return
     */
    @PostMapping("/member/register")
    public ResponseEntity<?> register(@RequestBody MemberDTO memberDTO){
            // 임시 회원 정보를 redis에 저장
//        registerService.register(memberDTO);

            memberService.register(memberDTO);
            return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * 로그인
     */
    @PostMapping("/member/login")
    public ResponseEntity<?> login(@ModelAttribute MemberDTO memberDTO){
        log.debug("login()");

        // 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(memberDTO.getLoginId(), memberDTO.getPassword())
        );

        SecurityConfig.CustomUserDetails userDetails = (SecurityConfig.CustomUserDetails) authentication.getPrincipal();
        log.debug("인증받은 회원 아이디 - {}, 이메일 - {}", userDetails.getUsername(), userDetails.getEmail());

        // 인증에 성공하면 AccessToken, RefreshToken 발급
        return ResponseEntity.ok("로그인 성공");
    }
}
