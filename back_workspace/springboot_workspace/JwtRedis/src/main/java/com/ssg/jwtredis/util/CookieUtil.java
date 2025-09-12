package com.ssg.jwtredis.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    /**
     * 쿠키 생성
     * refreshToken을 보안을 위해 httpOnly를 true로 하여, JS에서 접근 불가하도록 설정
     * @param response
     * @param token
     * @param maxAgeSec
     */
    public static void setRefreshCookie(HttpServletResponse response, String token, int maxAgeSec){
        Cookie cookie = new Cookie("Refresh", token);
        cookie.setHttpOnly(true); // JS 접근 불가
        cookie.setSecure(false); // https 비허용 (개발용)
        cookie.setPath("/"); // 클라이언트의 모든 경로에서 쿠키 사용 가능
        cookie.setMaxAge(maxAgeSec); // 유효기간

        response.addCookie(cookie); // 응답 시 쿠키로 전송
    }

    /**
     * 쿠키 삭제
     * @param response
     */
    public static void clearRefreshCookie(HttpServletResponse response){
        Cookie cookie = new Cookie("Refresh", "");
        cookie.setHttpOnly(true); // JS 접근 불가
        cookie.setSecure(false); // https 비허용 (개발용)
        cookie.setPath("/"); // 클라이언트의 모든 경로에서 쿠키 사용 가능
        cookie.setMaxAge(0); // 유효기간

        response.addCookie(cookie); // 응답 시 쿠키로 전송
    }
}
