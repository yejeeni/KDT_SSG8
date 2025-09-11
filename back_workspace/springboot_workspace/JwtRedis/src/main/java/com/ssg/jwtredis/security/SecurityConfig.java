package com.ssg.jwtredis.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 비밀번호 암호화 인코더
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(); // 비밀번호 결과물에 salt가 포함되어 있음
    }

    /**
     * 프로그래밍 방식 인증을 위한 authenticationManager
     * @param configuration
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    /**
     * 보안 필터 체인 설정
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 사용 X
                .sessionManagement(session->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 시큐리티에 검증된 회원을 직접 알림
                .securityContext(sc->sc
                        .requireExplicitSave(false))
                // 기본 로그인폼 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // 기본 로그아웃 비활성화
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/member/register.html", "/member/register", "/member/login.html", "/member/login").permitAll()
                        .requestMatchers("/member/refresh", "/member/logout", "/index.html", "/member/myinfo").permitAll()
                        .anyRequest().authenticated())
                .build();
    }
}
