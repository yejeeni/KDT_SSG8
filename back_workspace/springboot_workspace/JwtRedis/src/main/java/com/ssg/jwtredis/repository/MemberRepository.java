package com.ssg.jwtredis.repository;

import com.ssg.jwtredis.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Member 리포지토리
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

    /**
     * 로그인 id로 member 조회
     * @param loginId
     * @return
     */
    public Member findByLoginId(String loginId);

}
