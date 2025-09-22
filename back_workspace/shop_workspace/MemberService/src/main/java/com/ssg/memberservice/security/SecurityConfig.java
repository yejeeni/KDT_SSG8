package com.ssg.memberservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf->csrf.disable())
        .formLogin(form -> form.disable())
        .httpBasic(basic->basic.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/memberapp/oauth2/**").permitAll()
            .anyRequest().authenticated())
        .oauth2Login(oauth->oauth
            .defaultSuccessUrl("http://localhost:7777/memberapp/login/result", true)
        );
    return http.build();
  }
}
