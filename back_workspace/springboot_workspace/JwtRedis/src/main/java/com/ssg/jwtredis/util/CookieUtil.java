package com.ssg.jwtredis.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public static void setRefreshCookie(HttpServletResponse response, String token, int maxAgeSec){
        Cookie cookie = new Cookie("Refresh", token);
        cookie.setHttpOnly(true); // JS 접근 불가
        cookie.setSecure(false); // https 비허용 (개발용)
        cookie.setPath("/"); // 클라이언트의 모든 경로에서 쿠키 사용 가능
        cookie.setMaxAge(maxAgeSec); // 유효기간

        response.addCookie(cookie);
    }

    public static void clearRefreshCookie(HttpServletResponse response, String token, int maxAgeSec){
        Cookie cookie = new Cookie("Refresh", "");
        cookie.setHttpOnly(true); // JS 접근 불가
        cookie.setSecure(false); // https 비허용 (개발용)
        cookie.setPath("/"); // 클라이언트의 모든 경로에서 쿠키 사용 가능
        cookie.setMaxAge(0); // 유효기간

        response.addCookie(cookie);
    }
}
