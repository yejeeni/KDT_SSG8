package com.ssg.jwtredis.controller;

import com.ssg.jwtredis.dto.LogoutRequest;
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
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;

/**
 * 회원 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

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

            // 1) Spring Security를 통한 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(memberDTO.getLoginId(), memberDTO.getPassword())
            );

            // 인증된 사용자 정보 추출
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String userId = userDetails.getUsername();

            log.info("인증 성공 - userId: {}, email: {}, role: {}", userId, userDetails.getEmail(), userDetails.getRoleName());

            // 2) 사용자 토큰 버전 조회
            int userVersion = redisTokenService.currentUserVersion(userId);

            // 사용자 버전이 없으면 1로 초기화
            if (userVersion == 0) {
                userVersion = redisTokenService.incrementUserVersion(userId);
                log.debug("새 사용자 버전 생성 - userId: {}, version: {}", userId, userVersion);
            }

            // 3) JWT 토큰 발급
            String deviceId = memberDTO.getDeviceId();
            if (deviceId == null || deviceId.trim().isEmpty()) {
                deviceId = "default-device"; // 기본 디바이스 ID
            }

            // AccessToken 생성 (짧은 유효기간)
            String accessToken = jwtUtil.createAccessToken(userId, userVersion, deviceId);

            // RefreshToken 생성 (긴 유효기간)
            String refreshToken = jwtUtil.createRefreshToken(userId, deviceId);

            // 4) 리프레시 토큰 Redis 저장 및 쿠키 설정
            // 리프레시 토큰을 Redis에 저장
            long refreshTokenTTL = refreshDays * 24 * 60 * 60; // 일 -> 초 변환
            redisTokenService.storeRefreshToken(userId, deviceId, refreshToken, refreshTokenTTL);

            // 리프레시 토큰을 보안 쿠키에 설정
            CookieUtil.setRefreshCookie(response, refreshToken, (int) refreshTokenTTL);

            // 5) 액세스 토큰 만료시간 계산
            Claims claims = jwtUtil.getClaims(accessToken);
            long expireTime = jwtUtil.getExpireTime(claims);

            log.info("로그인 성공 - userId: {}, deviceId: {}, accessToken 만료: {}", userId, deviceId, expireTime);

            // 6) 응답 반환
            return ResponseEntity.ok(new TokenResponse(accessToken, expireTime));
        }
        catch (Exception e) {
            log.error("로그인 실패 - loginId: {}, error: {}", memberDTO.getLoginId(), e.getMessage());
            return ResponseEntity.ok(e);
        }
    }

    /**
     * 토큰 재발급
     * @ CookieValue(value="쿠키명", required=true/false)
     * 클라이언트 요청 헤더에 포함된 쿠키 항목에서 특정 쿠키 이름을 찾아, 컨트롤러 메서드의 파라미터에 주입
     * required == true : 쿠키가 없으면 400 에러
     * required == false : 에러 X. 쿠키가 없으면 null
     */
    @PostMapping("/member/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value="Refresh", required = false) String refreshToken,
                                     @RequestBody MemberDTO memberDTO, HttpServletResponse response){
        try{
            // 쿠키가 없다면 401 error
            if (StringUtils.hasText(refreshToken)){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "no refresh cookie"));
            }
            // RefreshToken이 유효한지 검증
            Jws<Claims> jws=jwtUtil.parseToken(refreshToken);
            Claims claims=jws.getBody();
            String userId=claims.getSubject(); //userId

            // redis와 일치여부를 판단
            if(!redisTokenService.matchesRefreshToken(userId, memberDTO.getDeviceId(), refreshToken)){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error","refresh not matched"));
            }
            log.debug("기존 Refresh Token 유효함");

            // redis에서 관리중인 userVersion을 가져오기
            int version = redisTokenService.currentUserVersion(userId);

            //보안상 안전성을 위해 AccessToken만 발급하지 말고, RefreshToken도 함께 갱신
            String newAccessToken=jwtUtil.createAccessToken(userId,version, memberDTO.getDeviceId());
            String newRefreshToken=jwtUtil.createRefreshToken(userId, memberDTO.getDeviceId());

            log.debug("newAccessToken = {}", newAccessToken);
            log.debug("newRefreshToken = {}", newRefreshToken);

            // RefreshToken 새롭게 발급되었으므로 기존 redis가 보관하고 있던 refreshToken을 제거하고 새롭게 추가
            redisTokenService.deleteRefreshToken(userId, memberDTO.getDeviceId());

            long rtTtlSec=refreshDays * (24*60*60);
            redisTokenService.storeRefreshToken(userId, memberDTO.getDeviceId(), newRefreshToken,rtTtlSec);

            // 보안 처리된 쿠키에 refreshToken 담기
            CookieUtil.setRefreshCookie(response, newRefreshToken, (int)rtTtlSec);

            // AccessToken을 응답 body에 넣기
            return ResponseEntity.ok(Map.of("accessToken",newAccessToken));

        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error","refresh failed"));
        }
    }

    /**
     * 회원 정보 요청 처리
     * @return
     */
    @GetMapping("/member/myinfo")
    public ResponseEntity<?> myinfo() {
        return ResponseEntity.ok("인증받은 회원입니다.");
    }

    /**
     * 로그아웃 요청 처리
     * 1) 로그아웃을 요청하는 클라이언트의 AccessToken을 블랙리스트로 등록
     * 2) 회원으로써 서비스 이용을 중단 요청이기에 Redis 에 등록된 RefreshToken 삭제
     * 3) 쿠키에 들어있는 RefreshToken 삭제
    */
    @PostMapping("/member/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request, HttpServletResponse response){
        try {
            // 1) 요청 데이터의 유효성 검사
            // 보안상 토큰이 유효하든 무효하든, 토큰 유효성 정보를 외부에 노출하지 않기 위해 항상 성공 응답을 반환
            if (request == null || !StringUtils.hasText(request.getAccessToken())
                    || !StringUtils.hasText(request.getDeviceId())) {
                return ResponseEntity.noContent().build(); // 204
            }

            // 2) AccessToken에서 사용자 정보 추출
            Jws<Claims> jws = jwtUtil.parseToken(request.getAccessToken());
            Claims claims = jws.getPayload();
            String userId = claims.getSubject();
            String jti = claims.getId();

            // 3) JWT 만료시간까지 남은 시간 TTL 계산
            long exp = claims.getExpiration().toInstant().getEpochSecond();
            long now = Instant.now().getEpochSecond();
            long ttl = Math.max(0, exp - now); // 음수가 나오면 0으로 처리

            // 4) AccessToken을 redis에 남은 만료시간만큼 블랙리스트에 등록
            // SET bl:access:<JTI> 45
            redisTokenService.registerBlackList(jti, ttl);

            // 5) Redis에서 해당 기기의 RefreshToken 삭제
            redisTokenService.deleteRefreshToken(userId, request.getDeviceId());

            // 6) 브라우저 쿠키에서 RefreshToken 삭제
            CookieUtil.clearRefreshCookie(response);

            return ResponseEntity.noContent().build();
        } catch (JwtException | IllegalArgumentException e) { // JWT 관련 모든 예외 (만료, 형식 오류 등)
            CookieUtil.clearRefreshCookie(response);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.warn("로그아웃 처리 중 예외 발생", e);
            CookieUtil.clearRefreshCookie(response);
            return ResponseEntity.noContent().build();
        }
    }
}
