package com.ticket.god_of_ticketing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ticket.god_of_ticketing.domain.Lecture;
import com.ticket.god_of_ticketing.repository.LectureRepository;

@SpringBootTest
public class LectureServiceTest {

    @Autowired
    private LectureService lectureService;

    @Autowired
    private LectureRepository lectureRepository;

    @Test
    @DisplayName("동시에 100명이 수강신청을 하면, 정확히 30명만 성공해야 한다.")
    void simultaneous_registration_test() throws InterruptedException {
        // given
        int threadCount = 100; // 100명이 동시 접속
        // 멀티스레드 작업을 도와주는 녀석 (32개 스레드 풀)
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        // 100개의 요청이 끝날 때까지 기다려주는 장치
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 테스트를 위해 미리 생성된 강의 ID (DataInitializer에서 만든 1번 강의)
        Long lectureId = 1L;

        // when (100번 반복)
        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    lectureService.apply(userId, lectureId); // 수강신청 시도!
                } catch (Exception e) {
                    // 정원 초과 예외 등은 그냥 무시 (로그만 찍음)
                    // System.out.println(e.getMessage()); 
                } finally {
                    latch.countDown(); // 작업 끝날 때마다 카운트 1 감소
                }
            });
        }

        latch.await(); // 0이 될 때까지(모든 스레드가 끝날 때까지) 대기

        // then
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
        
        // 💣 기대값은 30이지만, 실제로는 30보다 큰 값(초과 신청)이 나오거나 엉망일 것입니다.
        // assertEquals(30, lecture.getCurrentCapacity()); 이 코드는 실패해야 정상입니다!
        System.out.println("최종 신청 인원: " + lecture.getCurrentCapacity());
        
        // 우리는 실패를 확인하고 싶으니, 일부러 '실패해야 하는' 검증을 넣습니다.
        assertEquals(30, lecture.getCurrentCapacity());
    }
}