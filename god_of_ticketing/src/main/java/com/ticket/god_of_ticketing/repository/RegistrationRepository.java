package com.ticket.god_of_ticketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ticket.god_of_ticketing.domain.Registration;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    // 필요한 경우 쿼리 메서드 추가 가능
    // 예: 특정 유저가 이미 신청했는지 확인
    boolean existsByUserIdAndLectureId(Long userId, Long lectureId);
}