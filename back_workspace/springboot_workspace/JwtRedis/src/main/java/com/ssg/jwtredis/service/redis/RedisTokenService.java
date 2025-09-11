package com.ssg.jwtredis.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 기반 JWT 토큰 상태 관리 서비스
 *
 * Redis 키 설계:
 * 1) 블랙리스트: bl:access:<jti> - 로그아웃된 토큰 관리
 * 2) 사용자 버전: uv:<userId> - 전역 로그아웃을 위한 버전 관리
 * 3) 리프레시 토큰: rt:<userId>:<deviceId> - 리프레시 토큰 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate redis;

    /**
     * 사용자의 현재 버전 조회
     *
     * 버전 시스템의 목적:
     * - JWT + Redis 환경에서 블랙리스트만으로는 모든 토큰을 무효화하기 어려움
     * - 사용자가 비밀번호 변경, 전체 기기 로그아웃 시 기존 토큰들을 한번에 무효화
     * - 토큰 페이로드의 ver과 Redis의 uv:<userId> 값을 비교하여 검증
     *
     * 예시:
     * - 토큰 생성 시: ver=1
     * - 비밀번호 변경 후: Redis uv:user123 = 2
     * - 결과: ver=1인 모든 토큰이 무효화됨 (1 < 2)
     *
     * @param userId 사용자 ID
     * @return 현재 사용자 버전 (없으면 0)
     */
    public int currentUserVersion(String userId) {
        String key = "uv:" + userId;
        String version = redis.opsForValue().get(key);

        log.debug("사용자 버전 조회 - userId: {}, version: {}", userId, version);

        // Redis에서 GET uv:<userId> 명령 수행
        // 키가 없으면 null 반환, 있으면 저장된 문자열 반환
        return (version == null) ? 0 : Integer.parseInt(version);
    }

    /**
     * 사용자 버전 증가 (전역 로그아웃 시 사용)
     *
     * @param userId 사용자 ID
     * @return 증가된 새 버전
     */
    public int incrementUserVersion(String userId) {
        String key = "uv:" + userId;
        Long newVersion = redis.opsForValue().increment(key);

        log.info("사용자 버전 증가 - userId: {}, newVersion: {}", userId, newVersion);

        return newVersion.intValue();
    }

    /**
     * AccessToken을 블랙리스트에 등록
     *
     * 블랙리스트 vs 버전 시스템:
     * - 블랙리스트: 토큰 단위로 개별 차단 (로그아웃 시 사용)
     * - 버전 시스템: 사용자 단위로 모든 토큰 일괄 차단 (비밀번호 변경 등)
     *
     * @param jti 토큰의 고유 ID (JWT ID)
     * @param ttlSeconds 토큰의 남은 유효시간 (초)
     */
    public void registerBlackList(String jti, long ttlSeconds) {
        // 이미 만료된 토큰은 블랙리스트에 등록할 필요 없음
        if (ttlSeconds <= 0) {
            log.debug("토큰이 이미 만료됨 - jti: {}, ttl: {}", jti, ttlSeconds);
            return;
        }

        String key = "bl:access:" + jti;

        // Redis SETEX 명령: SETEX bl:access:<jti> <ttlSeconds> "1"
        // TTL이 지나면 자동으로 키가 삭제됨
        redis.opsForValue().set(key, "1", Duration.ofSeconds(ttlSeconds));

        log.info("토큰 블랙리스트 등록 - jti: {}, ttl: {}초", jti, ttlSeconds);
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     *
     * @param jti 토큰의 고유 ID
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isBlackList(String jti) {
        String key = "bl:access:" + jti;

        // Redis EXISTS 명령: EXISTS bl:access:<jti>
        // 반환값: 1(키 존재) 또는 0(키 없음)
        // Spring Data Redis가 자동으로 Boolean으로 변환
        Boolean exists = redis.hasKey(key);

        boolean isBlacklisted = exists != null && exists;

        if (isBlacklisted) {
            log.warn("블랙리스트 토큰 감지 - jti: {}", jti);
        }

        return isBlacklisted;
    }

    /**
     * 리프레시 토큰 저장
     *
     * @param userId 사용자 ID
     * @param deviceId 디바이스 ID
     * @param refreshToken 리프레시 토큰
     * @param ttlSeconds 토큰 유효시간 (초)
     */
    public void storeRefreshToken(String userId, String deviceId, String refreshToken, long ttlSeconds) {
        String key = "rt:" + userId + ":" + deviceId;

        redis.opsForValue().set(key, refreshToken, Duration.ofSeconds(ttlSeconds));

        log.info("리프레시 토큰 저장 - userId: {}, deviceId: {}, ttl: {}초", userId, deviceId, ttlSeconds);
    }

    /**
     * 리프레시 토큰 조회
     *
     * @param userId 사용자 ID
     * @param deviceId 디바이스 ID
     * @return 저장된 리프레시 토큰 (없으면 null)
     */
    public String getRefreshToken(String userId, String deviceId) {
        String key = "rt:" + userId + ":" + deviceId;
        String refreshToken = redis.opsForValue().get(key);

        log.debug("리프레시 토큰 조회 - userId: {}, deviceId: {}, found: {}",
                userId, deviceId, refreshToken != null);

        return refreshToken;
    }

    /**
     * 리프레시 토큰 삭제 (로그아웃 시 사용)
     *
     * @param userId 사용자 ID
     * @param deviceId 디바이스 ID
     */
    public void deleteRefreshToken(String userId, String deviceId) {
        String key = "rt:" + userId + ":" + deviceId;
        Boolean deleted = redis.delete(key);

        log.info("리프레시 토큰 삭제 - userId: {}, deviceId: {}, deleted: {}",
                userId, deviceId, deleted);
    }

    /**
     * 사용자의 모든 리프레시 토큰 삭제 (전체 기기 로그아웃 시 사용)
     *
     * @param userId 사용자 ID
     */
    public void deleteAllRefreshTokens(String userId) {
        String pattern = "rt:" + userId + ":*";

        // Redis KEYS 명령으로 패턴에 맞는 모든 키 찾기
        // 프로덕션에서는 SCAN 사용 권장 (KEYS는 블로킹 명령)
        var keys = redis.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
            log.info("모든 리프레시 토큰 삭제 - userId: {}, 삭제된 토큰 수: {}", userId, keys.size());
        }
    }

    /**
     * 토큰 관련 통계 조회 (모니터링 용도)
     *
     * @param userId 사용자 ID
     * @return 사용자의 활성 토큰 개수
     */
    public long getActiveTokenCount(String userId) {
        String pattern = "rt:" + userId + ":*";
        var keys = redis.keys(pattern);

        long count = (keys != null) ? keys.size() : 0;
        log.debug("활성 토큰 개수 - userId: {}, count: {}", userId, count);

        return count;
    }
}