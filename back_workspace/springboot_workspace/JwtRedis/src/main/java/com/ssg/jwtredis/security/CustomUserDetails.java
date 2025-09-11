package com.ssg.jwtredis.security;

import com.ssg.jwtredis.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security의 UserDetails 인터페이스를 구현한 사용자 상세 정보 클래스
 *
 * Spring Security에서 인증된 사용자 정보를 표현하기 위해 사용
 */
public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    /**
     * 사용자 권한 반환
     *
     * ROLE_ 접두어를 사용하는 이유:
     * 1) Spring Security에서는 ROLE_ 접두어가 붙은 문자열을 권한으로 인식
     * 2) hasRole("ADMIN") 사용 시 실제로는 ROLE_ADMIN 권한을 확인함
     *
     * 사용 예시:
     * (1) URL 접근 제어:
     *     http.authorizeHttpRequests(auth -> auth
     *         .requestMatchers("/admin/**").hasRole("ADMIN")
     *         .requestMatchers("/store/**").hasRole("STORE")
     *         .requestMatchers("/member/**").hasRole("MEMBER")
     *     );
     *
     * (2) 메서드 레벨 보안:
     *     @PreAuthorize("hasRole('ADMIN')")
     *     public void removeMember() {}
     *
     * (3) 런타임 권한 확인:
     *     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     *     Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
     *     for(GrantedAuthority authority : authorities) {
     *         log.debug("권한: {}", authority.getAuthority());
     *     }
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + member.getRole().getRoleName())
        );
    }

    /**
     * 사용자 식별자 반환 (로그인 ID)
     */
    @Override
    public String getUsername() {
        return member.getLoginId(); // 또는 member.getId()
    }

    /**
     * 사용자 비밀번호 반환
     */
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    /**
     * 계정 만료 여부 확인
     * @return true면 계정이 유효함
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 로직이 없다면 true
    }

    /**
     * 계정 잠금 여부 확인
     * @return true면 계정이 잠금되지 않음
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 로직이 없다면 true
    }

    /**
     * 자격 증명 만료 여부 확인 (비밀번호 만료 등)
     * @return true면 자격 증명이 유효함
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 로직이 없다면 true
    }

    // ======================================
    // 추가 편의 메서드들
    // ======================================

    /**
     * 사용자 이메일 반환
     */
    public String getEmail() {
        return member.getEmail();
    }

    /**
     * 사용자 역할명 반환 (ROLE_ 접두어 없이)
     */
    public String getRoleName() {
        return member.getRole().getRoleName();
    }

    /**
     * 사용자 이름 반환
     */
    public String getName() {
        return member.getName();
    }

    /**
     * Member 엔티티 반환
     */
    public Member getMember() {
        return member;
    }

    /**
     * 사용자 ID 반환 (PK)
     */
    public int getUserId() {
        return member.getMemberId();
    }
}