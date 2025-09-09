package com.ssg.jwtredis.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Bcrypt 알고리즘 적용 비밀번호 생성 클래스
 */
public class PasswordGenerator {
    /**
     * 평문 password에 BCrypt 알고리즘을 적용
     * @param password
     * @return
     */
    public static String convert(String password){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    public static void main(String[] args){
        System.out.println(PasswordGenerator.convert("1234"));
    }
}
