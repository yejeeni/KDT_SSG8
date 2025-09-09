package com.ssg.jwtredis.service.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssg.jwtredis.dto.MemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * Redis 관련 로직 클래스
 * 임시 가입 정보 저장 및 검증
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecureRandom random = new SecureRandom();
    // 데이터를 문자열화 시켜서 넣을 때, 문자열화 작업을 처리해주는 라이브러리
    private final StringRedisTemplate redis;

    // TTL 3분
    private static final Duration PENDING_TTL = Duration.ofMinutes(3);

    /**
     * redis에 사용될 키 규칙 설계
     * @param email
     * @return
     */
    private String pendingKey(String email){
        return "pending:" + email;
    }

    /**
     * 이메일을 조회하기 위한 키 설계
     * @param code
     * @return
     */
    private String codeKey(String code){
        return "code:"+code;
    }

    /**
     * 이메일 인증코드 생성
     * 6자리
     */
    public String generateEmailValidCode(){
        return String.format("%06d", random.nextInt(1_000_000)); // 6자리 랜덤값 반환
    }

    /**
     * 임시 가입
     * redis에 쓰기
     */
    public void savePending(MemberDTO memberDTO) {
        // Member 객체가 redis에 저장되려면 문자열화되어야 함
        try {
            String json = objectMapper.writeValueAsString(memberDTO);

            // Redis에 임시 회원가입 정보 저장
            redis.opsForValue().set(pendingKey(memberDTO.getEmail()), json, PENDING_TTL);

            // 이메일을 찾을 수 있도록 인덱스 생성
            redis.opsForValue().set(codeKey(memberDTO.getCode()), memberDTO.getEmail(), PENDING_TTL);

            String pendingKeyStr = pendingKey(memberDTO.getEmail());
            String codeKeyStr = codeKey(memberDTO.getCode());
            // 저장된 내용 확인
            String savedMember = redis.opsForValue().get(pendingKeyStr);
            String savedEmail = redis.opsForValue().get(codeKeyStr);

            log.debug("저장된 회원 정보: {}", savedMember);
            log.debug("저장된 이메일: {}", savedEmail);

//            // 인증코드 생성
//            String verificationCode = generateEmailValidCode();
//            // 이메일과 매핑 저장
//            redis.opsForValue().set(codeKey(verificationCode), member.getEmail(), PENDING_TTL);
//
//            return verificationCode; // 생성된 인증코드 반환
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
