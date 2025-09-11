package com.ssg.jwtredis.filter;

import com.ssg.jwtredis.service.redis.RedisTokenService;
import com.ssg.jwtredis.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 *
 * 매 요청마다 실행되어 다음 과정을 수행:
 * 1) Authorization 헤더에서 Bearer 토큰 추출
 * 2) JWT 토큰의 서명, 만료시간 등 유효성 검증
 * 3) Redis 블랙리스트 확인 (로그아웃된 토큰인지 체크)
 * 4) 사용자 버전 검증 (전역 로그아웃 처리)
 * 5) 모든 검증 통과 시 Spring Security에 인증 정보 등록
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            log.debug("JWT 인증 필터 실행 - URI: {}", request.getRequestURI());

            /*----------------------------
            1단계: Authorization 헤더에서 JWT 토큰 추출
            ----------------------------*/
            String authHeader = request.getHeader("Authorization");

            // Bearer 토큰이 있는지 확인
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // "Bearer " 이후 부분이 실제 토큰
                log.debug("JWT 토큰 추출 완료");

                /*----------------------------
                2단계: JWT 토큰 파싱 및 유효성 검증
                - 서명 조작 여부 확인
                - 토큰 만료 시간 확인
                - 토큰 구조 검증
                ----------------------------*/
                Jws<Claims> jws = jwtUtil.parseToken(token);
                Claims claims = jws.getPayload();
                log.debug("JWT 토큰 파싱 및 검증 완료");

                /*----------------------------
                3단계: Claims에서 필요한 정보 추출
                ----------------------------*/
                String jti = jwtUtil.getJti(claims); // JWT 고유 ID (UUID)
                String userId = jwtUtil.getUserId(claims); // 로그인한 사용자 ID
                int userVersion = jwtUtil.getVersion(claims); // 사용자 토큰 버전
                String deviceId = jwtUtil.getDeviceId(claims); // 디바이스 ID

                log.debug("토큰 정보 - userId: {}, jti: {}, version: {}, deviceId: {}", userId, jti, userVersion, deviceId);

                /*----------------------------
                4단계: Redis 블랙리스트 확인
                로그아웃된 토큰인지 확인
                ----------------------------*/
                if (redisTokenService.isBlackList(jti)) {
                    log.warn("블랙리스트에 등록된 토큰 사용 시도 - jti: {}", jti);
                    sendUnauthorizedResponse(response, "로그아웃된 토큰");
                    return;
                }

                /*----------------------------
                5단계: 사용자 버전 검증
                전역 로그아웃 기능을 위한 버전 확인
                (사용자가 모든 디바이스에서 로그아웃하면 버전이 변경됨)
                ----------------------------*/
                int currentUserVersion = redisTokenService.currentUserVersion(userId);
                if (currentUserVersion != userVersion) {
                    log.warn("토큰 버전 불일치 - userId: {}, 토큰버전: {}, 현재버전: {}", userId, userVersion, currentUserVersion);
                    sendUnauthorizedResponse(response, "토큰 버전이 일치하지 않습니다.");
                    return;
                }

                /*----------------------------
                6단계: Spring Security에 인증 정보 등록
                모든 검증을 통과했으므로 인증된 사용자로 처리
                ----------------------------*/
                // 사용자 권한 설정 (기본적으로 ROLE_USER 권한 부여)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId, // principal: 인증된 사용자 ID
                                null, // credentials: 비밀번호 (JWT이므로 null)
                                List.of(new SimpleGrantedAuthority("ROLE_USER")) // authorities: 사용자 권한
                        );

                // SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Spring Security 인증 완료 - userId: {}", userId);
            }

            /*----------------------------
            7단계: 다음 필터로 요청 전달
            인증 처리가 완료되었으므로 요청 흐름을 계속 진행
            ----------------------------*/
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // JWT 토큰 만료
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            sendUnauthorizedResponse(response, "토큰이 만료되었습니다.");

        } catch (JwtException e) {
            // JWT 관련 예외 (잘못된 서명, 형식 오류 등)
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            sendUnauthorizedResponse(response, "유효하지 않은 토큰입니다.");

        } catch (Exception e) {
            // 기타 예외
            log.error("JWT 인증 필터에서 예외 발생", e);
            sendUnauthorizedResponse(response, "인증 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 401 Unauthorized 응답 전송 헬퍼 메서드
     *
     * @param response HTTP 응답 객체
     * @param message 에러 메시지
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(String.format("{\"error\": \"%s\"}", message));
        response.getWriter().flush();
    }

    /**
     * 특정 경로에 대해 필터를 적용하지 않을지 결정
     * 예: 로그인, 회원가입 등의 경로는 JWT 검증 불필요
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // JWT 검증이 필요 없는 경로들 추가하면 됨
        return path.startsWith("");
    }
}