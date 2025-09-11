package com.ssg.jwtredis.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 발급/검증 전용 유틸리티
 *
 * AccessToken: 인증을 받았음을 증명하는 용도의 토큰 (짧은 만료시간)
 * RefreshToken: AccessToken 재발급을 위한 검증용 토큰 (긴 만료시간)
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final String issuer;
    private final long accessMinutes;
    private final long refreshDays;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-minutes}") long accessMinutes,
            @Value("${app.jwt.refresh-days}") long refreshDays) {

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessMinutes = accessMinutes;
        this.refreshDays = refreshDays;
    }

    /**
     * AccessToken 생성
     */
    public String createAccessToken(String userId, int userVersion, String deviceId) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessMinutes * 60);

        return Jwts.builder()
                .issuer(issuer)
                .subject(userId)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("ver", userVersion)
                .claim("deviceId", deviceId)
                .signWith(secretKey)
                .compact();
    }

    /**
     * RefreshToken 생성
     * 보안상 민감한 토큰이므로 유출 시 즉시 재발급 필요
     */
    public String createRefreshToken(String userId, String deviceId) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(refreshDays * 24 * 60 * 60);

        return Jwts.builder()
                .issuer(issuer)
                .subject(userId)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("deviceId", deviceId)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.debug("유효하지 않은 토큰 - {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 파싱 및 검증 (서명, 만료기간, 구조 검증)
     */
    public Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    /**
     * 토큰에서 Claims 추출
     */
    public Claims getClaims(String token) {
        return parseToken(token).getPayload();
    }

    // =================================================================
    // Claims에서 정보 추출 메서드들
    // =================================================================

    /**
     * 사용자 ID 추출
     */
    public String getUserId(Claims claims) {
        return claims.getSubject();
    }

    /**
     * 토큰 고유 ID(JTI) 추출
     */
    public String getJti(Claims claims) {
        return claims.getId();
    }

    /**
     * 사용자 버전 추출
     */
    public int getVersion(Claims claims) {
        Object version = claims.get("ver");
        return (version == null) ? 0 : (Integer) version;
    }

    /**
     * 디바이스 ID 추출
     */
    public String getDeviceId(Claims claims) {
        Object deviceId = claims.get("deviceId");
        return (deviceId == null) ? "" : deviceId.toString();
    }

    /**
     * 토큰 만료시간 추출 (Unix timestamp)
     */
    public long getExpireTime(Claims claims) {
        Date expiration = claims.getExpiration();
        return (expiration == null) ? 0L : expiration.toInstant().getEpochSecond();
    }

    /**
     * 토큰 발급시간 추출 (Unix timestamp)
     */
    public long getIssuedTime(Claims claims) {
        Date issuedAt = claims.getIssuedAt();
        return (issuedAt == null) ? 0L : issuedAt.toInstant().getEpochSecond();
    }

    // =================================================================
    // 편의 메서드들 - 토큰에서 직접 정보 추출
    // =================================================================

    /**
     * 토큰에서 사용자 ID 직접 추출
     */
    public String getUserIdFromToken(String token) {
        return getUserId(getClaims(token));
    }

    /**
     * 토큰에서 디바이스 ID 직접 추출
     */
    public String getDeviceIdFromToken(String token) {
        return getDeviceId(getClaims(token));
    }

    /**
     * 토큰에서 사용자 버전 직접 추출
     */
    public int getVersionFromToken(String token) {
        return getVersion(getClaims(token));
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 토큰 만료까지 남은 시간 (초)
     */
    public long getTimeUntilExpiration(String token) {
        try {
            Claims claims = getClaims(token);
            long expireTime = getExpireTime(claims);
            long currentTime = Instant.now().getEpochSecond();
            return Math.max(0, expireTime - currentTime);
        } catch (Exception e) {
            return 0;
        }
    }
}