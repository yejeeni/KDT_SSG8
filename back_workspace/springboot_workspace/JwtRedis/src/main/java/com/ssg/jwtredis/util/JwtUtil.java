package com.ssg.jwtredis.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

//@Component
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private long accessTokenExpiration; // 액세스 토큰 유효기간
    
    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration; // 리프레시 토큰 유효기간
    
    /**
     * SecretKey 생성
     */
    private SecretKey getSigningKey() {
        // secretKey 값을 바이트 배열로 변환하여 HMAC 키 생성
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token 발급
     */
    public String generateAccessToken(String username){
        return generateToken(username, accessTokenExpiration);
    }

    /**
     * Refresh Token 발급
     */
    public String generateRefreshToken(String username){
        return generateToken(username, refreshTokenExpiration);
    }

    /**
     * JWT 토큰 생성
     * 사용자 정보와 만료시간을 포함한 JWT 토큰을 생성
     *
     * @param username 토큰에 포함될 사용자 이름 (subject로 저장)
     * @param exp 토큰 만료 시간 (밀리초 단위, 현재시간 + exp 후 만료)
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateToken(String username, long exp) {
        return Jwts.builder()
                .subject(username) // setSubject → subject
                .issuedAt(new Date()) // setIssuedAt → issuedAt. 발급 시점
                .expiration(new Date(System.currentTimeMillis() + exp)) // setExpiration → expiration. 유효 기간
                .signWith(getSigningKey()) // signWith 간소화 (알고리즘 자동 선택. HMAC-SHA256으로 서명 (위변조 방지))
                .compact(); // JWT 문자열로 압축하여 반환

        // deprecated
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(new Date()) // 토큰의 발급 시점
//                .setExpiration(new Date(System.currentTimeMillis() + exp))
//                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
//                .compact();
    }

    /**
     * JWT 토근의 서명과 만료시간 검증
     * @param token
     * @return
     */
    public boolean validateToken(String token){
        try {
            // deprecated
//            Jwts.parserBuilder()
//                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
//                    .build()
//                    .parseClaimsJws(token);
            Jwts.parser()
                    .verifyWith(getSigningKey())// 서명 검증용 키 설정
                    .build()
                    .parseSignedClaims(token); // 토큰 파싱 및 검증
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * JWT 토큰에서 사용자명 추출
     * 토큰을 파싱하여 저장된 사용자명(subject)을 가져옴
     *
     * @param token 파싱할 JWT 토큰 문자열
     * @return 토큰에 저장된 사용자명
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser() // JWT 파서 생성
                .verifyWith(getSigningKey()) // 서명 검증을 위한 키 설정
                .build() // 파서 빌드
                .parseSignedClaims(token) // 토큰 파싱 및 서명 검증
                .getPayload() // 토큰의 페이로드(실제 데이터) 부분 가져오기
                .getSubject(); // 페이로드에서 subject(사용자명) 추출

        // deprecated
//        return Jwts.parser()
//                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
    }

    /**
     * 토큰에서 사용자 권한 정보 추출
     */
    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        // 권한 정보가 토큰에 없어도 기본 권한 반환
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
