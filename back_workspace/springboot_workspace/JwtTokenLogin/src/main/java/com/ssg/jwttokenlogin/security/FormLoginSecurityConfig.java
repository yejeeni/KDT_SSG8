package com.ssg.jwttokenlogin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

/**
 * Spring Security 설정 클래스
 * 인증, 인가, 로그인 처리 등의 보안 관련 설정 담당
 */
//@EnableWebSecurity
//@Configuration
@RequiredArgsConstructor
public class FormLoginSecurityConfig {

    // 시큐리티가 사용할 서비스 객체
    // UserDetailsService 인터페이스를 하나만 구현해두면 스프링 부트가 알아서 등록 가능
//    private final CustomUserDetailService customUserDetailService;


    /**
     * DaoAuthenticationProvider가 사용할 비밀번호 검증 인코더 등록
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(); // 비밀번호 결과물에 salt가 포함되어 있음
    }

    // 여기도 스프링 부트가 알아서 연결 가능
//    /**
//     * 아이디와 패스워드를 자동으로 비교해주는 Provider 등록
//     */
//    @Bean
//    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder){
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        // Provider가 사용할 서비스 객체
//        provider.setUserDetailsService(customUserDetailService);
//        // 사용할 비밀번호 인코더
//        provider.setPasswordEncoder(passwordEncoder);
//
//        return provider;
//    }
//

    /**
     * 인증 처리를 위한 AuthenticationManager 빈 등록
     * REST API 인증, JWT 토큰 검증에서 사용
     * @param configuration Spring Security 인증 설정
     * @return AuthenticationManager 인스턴스
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Spring Security 필터 체인 설정
     * CSRF 보호, 로그인 처리 권한 검사 등
     * @param httpSecurity
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable) // 사이트 간 위변조 방지 disable. form 로그인/세션이 아닌 REST/JWT로 인증 처리 시 보통 disable
                .formLogin(form -> form
                                .loginPage("/member/login.html") // 로그인 폼 url
                                .loginProcessingUrl("/member/login") // 시큐리티가 로그인을 처리할 url

                                // 동기 방식으로 요청이 들어올 때, 결과를 html로 보여줄 때 사용
//                        .defaultSuccessUrl("/index.html") // 로그인 성공 후 보여질 url

                                // 비동기 방식으로 요청이 들어오면 json으로 결과를 보여줘야 함. 컨트롤러를 작성하지 않고도 json 결과를 전송 가능
                                .successHandler((request, response, authentication) -> {
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    response.setContentType("application/json;charset=UTF-8");

                                    Map<String, String> result = Map.of(
                                            "result", "로그인 성공",
                                            "user", authentication.getName()
                                    );

                                    ObjectMapper objectMapper = new ObjectMapper();
                                    response.getWriter().write(objectMapper.writeValueAsString(result));
                                })
                                .failureHandler((request, response, exception) -> {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType("application/json;charset=UTF-8");

                                    Map<String, String> result = Map.of("result", "로그인 실패");

                                    ObjectMapper objectMapper = new ObjectMapper();
                                    response.getWriter().write(objectMapper.writeValueAsString(result));
                                })
                )
                .build();
    }
}
