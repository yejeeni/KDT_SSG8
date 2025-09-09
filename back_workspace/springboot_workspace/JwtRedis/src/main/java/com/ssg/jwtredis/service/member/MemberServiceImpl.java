package com.ssg.jwtredis.service.member;

import com.ssg.jwtredis.domain.Member;
import com.ssg.jwtredis.dto.MemberDTO;
import com.ssg.jwtredis.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 회원 서비스
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Override
    public Member register(MemberDTO memberDTO){
        // 가입 정보 검증
        validateDuplicateLoginId(memberDTO.getLoginId());
//        validateDuplicateEmail(memberDTO.getEmail());
        validateInput(memberDTO);

        Member member = createMember(memberDTO);
        return memberRepository.save(member);
    }

    /**
     * 중복 아이디 검사
     * @param loginId
     */
    private void validateDuplicateLoginId(String loginId) {
        Member findMember = memberRepository.findByLoginId(loginId);
        if (findMember != null) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다: " + loginId);
        }
    }

    /**
     * 중복 이메일 검사
     * @param email
     */
//    private void validateDuplicateEmail(String email) {
//        Member findMember = memberRepository.findByEmail(email);
//        if (findMember != null) {
//            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + email);
//        }
//    }

    private void validateInput(MemberDTO memberDTO) {
        if (memberDTO.getLoginId() == null || memberDTO.getLoginId().trim().isEmpty()) {
            throw new IllegalArgumentException("로그인 ID는 필수입니다");
        }
        if (memberDTO.getPassword() == null || memberDTO.getPassword().length() < 6) {
            throw new IllegalArgumentException("비밀번호는 6자 이상이어야 합니다");
        }
        if (memberDTO.getName() == null || memberDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
        if (memberDTO.getEmail() == null || memberDTO.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
    }

    private Member createMember(MemberDTO memberDTO) {
        Member member = new Member();
        member.setLoginId(memberDTO.getLoginId());
        member.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
        member.setName(memberDTO.getName());
        member.setEmail(memberDTO.getEmail());
        return member;
    }
}