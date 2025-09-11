package com.ssg.jwtredis.controller;

import com.ssg.jwtredis.dto.MemberDTO;
import com.ssg.jwtredis.dto.TokenResponse;
import com.ssg.jwtredis.security.CustomUserDetails;
import com.ssg.jwtredis.security.SecurityConfig;
import com.ssg.jwtredis.service.member.MemberService;
import com.ssg.jwtredis.service.member.RegisterService;
import com.ssg.jwtredis.service.redis.RedisTokenService;
import com.ssg.jwtredis.util.CookieUtil;
import com.ssg.jwtredis.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;

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

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;

    @Value("${app.jwt.access-minutes}")
    private int accessMinutes;

    @Value("${app.jwt.refresh-days}")
    private int refreshDays;

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
     *
     * @param memberDTO 로그인 정보 (loginId, password, deviceId)
     * @param response HTTP 응답 (리프레시 토큰 쿠키 설정용)
     * @return 액세스 토큰과 만료시간
     */
    @PostMapping("/member/login")
    public ResponseEntity<?> login(@RequestBody MemberDTO memberDTO, HttpServletResponse response) {
        try {
            log.info("로그인 시도 - loginId: {}, deviceId: {}", memberDTO.getLoginId(), memberDTO.getDeviceId());

            /*----------------------------
            1단계: Spring Security를 통한 인증
            ----------------------------*/
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(memberDTO.getLoginId(), memberDTO.getPassword())
            );

            // 인증된 사용자 정보 추출
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String userId = userDetails.getUsername();

            log.info("인증 성공 - userId: {}, email: {}, role: {}",
                    userId, userDetails.getEmail(), userDetails.getRoleName());

            /*----------------------------
            2단계: 사용자 토큰 버전 조회
            전역 로그아웃 기능을 위한 버전 관리
            ----------------------------*/
            int userVersion = redisTokenService.currentUserVersion(userId);

            // 사용자 버전이 없으면 1로 초기화
            if (userVersion == 0) {
                userVersion = redisTokenService.incrementUserVersion(userId);
                log.debug("새 사용자 버전 생성 - userId: {}, version: {}", userId, userVersion);
            }

            /*----------------------------
            3단계: JWT 토큰 발급
            ----------------------------*/
            String deviceId = memberDTO.getDeviceId();
            if (deviceId == null || deviceId.trim().isEmpty()) {
                deviceId = "default-device"; // 기본 디바이스 ID
            }

            // AccessToken 생성 (짧은 유효기간)
            String accessToken = jwtUtil.createAccessToken(userId, userVersion, deviceId);

            // RefreshToken 생성 (긴 유효기간)
            String refreshToken = jwtUtil.createRefreshToken(userId, deviceId);

            /*----------------------------
            4단계: 리프레시 토큰 Redis 저장 및 쿠키 설정
            ----------------------------*/
            // 리프레시 토큰을 Redis에 저장
            long refreshTokenTTL = refreshDays * 24 * 60 * 60; // 일 -> 초 변환
            redisTokenService.storeRefreshToken(userId, deviceId, refreshToken, refreshTokenTTL);

            // 리프레시 토큰을 보안 쿠키에 설정
            CookieUtil.setRefreshCookie(response, refreshToken, (int) refreshTokenTTL);

            /*----------------------------
            5단계: 액세스 토큰 만료시간 계산
            ----------------------------*/
            Claims claims = jwtUtil.getClaims(accessToken);
            long expireTime = jwtUtil.getExpireTime(claims);

            log.info("로그인 성공 - userId: {}, deviceId: {}, accessToken 만료: {}",
                    userId, deviceId, expireTime);

            /*----------------------------
            6단계: 응답 반환
            ----------------------------*/
            return ResponseEntity.ok(new TokenResponse(accessToken, expireTime));

        }
        catch (Exception e) {
            log.error("로그인 실패 - loginId: {}, error: {}", memberDTO.getLoginId(), e.getMessage());
            return ResponseEntity.ok(e);
        }
    }
}
