package com.ssg.jwtredis.service.member;

import com.ssg.jwtredis.domain.Member;
import com.ssg.jwtredis.repository.MemberRepository;
import com.ssg.jwtredis.security.SecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Member member = memberRepository.findByLoginId(username);

        if (member == null) {
            throw new UsernameNotFoundException("사용자 정보 없음");
        }

        return new SecurityConfig.CustomUserDetails(member);
    }
}
