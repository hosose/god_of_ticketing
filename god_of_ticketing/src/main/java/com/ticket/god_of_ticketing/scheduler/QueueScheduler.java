package com.ticket.god_of_ticketing.scheduler;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.ticket.god_of_ticketing.service.LectureService;
import com.ticket.god_of_ticketing.service.QueueService;

/*
 * 자원낭비를 최소로 해보고자 스케줄러의 속도 설정
 */

@Configuration
// @RequiredArgsConstructor // 롬복 안 되면 생성자 주입!
public class QueueScheduler implements SchedulingConfigurer {

    private final QueueService queueService;
    private final LectureService lectureService;

    // 🏎️ 고속 모드: 0.1초 (사람 있을 때)
    private static final int FAST_INTERVAL = 100;
    // 🐢 저속 모드: 3초 (사람 없을 때)
    private static final int SLOW_INTERVAL = 3000;

    // 현재 대기 시간 (기본값은 저속)
    private AtomicInteger currentDelay = new AtomicInteger(SLOW_INTERVAL);

    public QueueScheduler(QueueService queueService, LectureService lectureService) {
        this.queueService = queueService;
        this.lectureService = lectureService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
            // 1. 실행할 작업 (Runnable)
            () -> {
                processQueue(); 
            },
            // 2. 다음 실행 시간 결정 (Trigger)
            triggerContext -> {
                // 방금 끝난 시간 가져오기
                Instant lastCompletion = triggerContext.lastCompletion();
                if (lastCompletion == null) {
                    lastCompletion = Instant.now();
                }
                // "끝난 시간 + 현재 설정된 대기 시간" 뒤에 실행해라!
                return Date.from(lastCompletion.plusMillis(currentDelay.get())).toInstant();
            }
        );
    }

    // 실제 비즈니스 로직
    private void processQueue() {
        Long lectureId = 1L;
        long enterCount = 10;

        Collection<Long> users = queueService.getFirstTokens(lectureId, enterCount);

        if (users.isEmpty()) {
            // 💤 사람 없네? 천천히 돌자 (3초로 변경)
            if (currentDelay.get() != SLOW_INTERVAL) {
                System.out.println("💤 대기열 없음. 저속 모드 전환 (3000ms)");
                currentDelay.set(SLOW_INTERVAL);
            }
            return;
        }

        // 🔥 사람 있네? 빨리 돌자! (0.1초로 변경)
        if (currentDelay.get() != FAST_INTERVAL) {
            System.out.println("🔥 트래픽 감지! 고속 모드 전환 (100ms)");
            currentDelay.set(FAST_INTERVAL);
        }

        System.out.println("== 🏃‍♂️ 입장 시작! (" + users.size() + "명) ==");

        for (Long userId : users) {
            try {
                lectureService.apply(userId, lectureId);
                queueService.removeQueue(lectureId, userId);
                System.out.println("   -> 입장 성공: User " + userId);
            } catch (Exception e) {
                queueService.removeQueue(lectureId, userId);
            }
        }
    }
}