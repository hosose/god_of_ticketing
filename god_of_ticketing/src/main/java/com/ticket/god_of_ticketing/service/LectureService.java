package com.ticket.god_of_ticketing.service;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional; // 🚨 주의: 이걸 지우거나 주석처리 하세요!

import com.ticket.god_of_ticketing.domain.Lecture;
import com.ticket.god_of_ticketing.domain.Registration;
import com.ticket.god_of_ticketing.repository.LectureRepository;
import com.ticket.god_of_ticketing.repository.RegistrationRepository;

@Service
public class LectureService {

    private final LectureRepository lectureRepository;
    private final RegistrationRepository registrationRepository;
    private final RedissonClient redissonClient; // ✅ 추가됨

    // 생성자 주입
    public LectureService(LectureRepository lectureRepository, 
                          RegistrationRepository registrationRepository,
                          RedissonClient redissonClient) {
        this.lectureRepository = lectureRepository;
        this.registrationRepository = registrationRepository;
        this.redissonClient = redissonClient;
    }

    public Lecture create(String title, int maxCapacity) {
        return lectureRepository.save(new Lecture(title, maxCapacity));
    }

    /**
     * ✅ 분산 락(Distributed Lock) 적용
     * @Transactional을 락 안에서 범위를 최소화하거나, Facade 패턴을 써야 하지만
     * 일단 이해를 돕기 위해 락 안에서 수동으로 처리하는 구조로 갑니다.
     */
    public void apply(Long userId, Long lectureId) {
        // 1. 락 이름 정의 (강의 ID별로 잠금)
        RLock lock = redissonClient.getLock("lecture_lock:" + lectureId);

        try {
            // 2. 락 획득 시도 (최대 5초 기다리고, 1초 동안 점유)
            // tryLock(waitTime, leaseTime, unit)
            boolean available = lock.tryLock(5, 1, TimeUnit.SECONDS);

            if (!available) {
                System.out.println("락 획득 실패! 줄이 너무 깁니다.");
                return;
            }

            // ======= [여기서부터는 한 명씩만 들어옵니다] =======
            
            // 3. 강의 조회
            Lecture lecture = lectureRepository.findById(lectureId)
                    .orElseThrow(() -> new IllegalArgumentException("강의가 존재하지 않습니다."));

            // 4. 정원 체크
            if (lecture.getCurrentCapacity() >= lecture.getMaxCapacity()) {
                // System.out.println("마감되었습니다.");
                return; // 예외 던지면 테스트가 더러워지니 리턴 처리
            }

            // 5. 수강 내역 저장
            Registration registration = new Registration(userId, lectureId);
            registrationRepository.save(registration);

            // 6. 카운트 증가
            lecture.increaseCapacity();
            lectureRepository.save(lecture);
            
            System.out.println("성공! (User: " + userId + ")");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7. 락 해제 (필수!)
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}