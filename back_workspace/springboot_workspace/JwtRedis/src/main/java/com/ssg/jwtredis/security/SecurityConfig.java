package com.ssg.jwtredis.security;

import com.ssg.jwtredis.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    private final JwtUtil jwtUtil;

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
                        .requestMatchers("/member/register.html", "/member/register", "/member/login.html", "/member/login", "/member/refresh", "/member/logout").permitAll()
                        .anyRequest().authenticated())
                .build();
    }

    @RequiredArgsConstructor
    public static class CustomUserDetails implements UserDetails {

        private final Member member;

        /**
         * 권한 반환
         * 권한 사용처
         *      1. URL 접근 제어
         *          http.authroizeHttpRequest(auth->auth).requestMachers("/...").hasRole("ADMIN")
         *      2. 서비스나 컨트롤러 메서드에서 어노테이션을 붙여 제어 가능
         *          @ PreAuthorize("hasRole('ADMIN')") public void removeMember();
         *      3. 런타임 시 현재 유저가 보유한 권한을 확인
         *          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         *          Collection<? extends GrantedAuthority> auths = auth.getAuthorities();
         * @return
         */
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // role에 ROLE_ 접두어를 사용해야 시큐리티가 권한으로 인식
            return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().getRoleName()));
        }

        @Override
        public String getPassword() {
            return member.getPassword();
        }

        @Override
        public String getUsername() {
            return member.getName();
        }

        public String getEmail(){
            return member.getEmail();
        }

        public String getRoleName(){
            return member.getRole().getRoleName();
        }
    }
}
