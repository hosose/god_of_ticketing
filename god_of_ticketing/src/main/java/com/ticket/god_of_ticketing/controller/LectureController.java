package com.ticket.god_of_ticketing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticket.god_of_ticketing.service.LectureService;
import com.ticket.god_of_ticketing.service.QueueService;

import lombok.RequiredArgsConstructor;

@RestController // "나 웹 요청 받는 곳이야" 명찰
@CrossOrigin(origins = "*")
// @RequiredArgsConstructor // 롬복 안 되면 이것도 생성자 주입으로 바꿔야 합니다!
public class LectureController {

    private final LectureService lectureService;

    @Autowired
    private QueueService queueService;
    
    // ✅ [수동 추가] 생성자 주입 (롬복 미작동 대비)
    public LectureController(LectureService lectureService, QueueService queueService) {
        this.lectureService = lectureService;
        this.queueService = queueService;
    }

    /**
     * 수강신청 API
     * 주소 예시: POST /lectures/1/apply?userId=100
     */
    @PostMapping("/lectures/{lectureId}/apply")
    public String apply(@PathVariable("lectureId") Long lectureId, @RequestParam("userId") Long userId) {
        try {
//            lectureService.apply(userId, lectureId);
        	queueService.addQueue(lectureId, userId);
//            return "수강신청 성공! (User: " + userId + ")";
        	return "대기열 등록 완료! (User: " + userId + ") - 잠시 후 순번을 확인해주세요.";
        } catch (Exception e) {
            return "실패: " + e.getMessage();
        }
    }
    
 // 1. 대기열 등록 API
    @PostMapping("/lectures/{lectureId}/queue")
    public String enterQueue(@PathVariable("lectureId") Long lectureId, @RequestParam("userId") Long userId) {
        queueService.addQueue(lectureId, userId);
        return "대기열 등록 완료";
    }

    // 2. 내 순서 확인 API
    @GetMapping("/lectures/{lectureId}/queue/rank")
    public String checkRank(@PathVariable("lectureId") Long lectureId, @RequestParam("userId") Long userId) {
        Long rank = queueService.getRank(lectureId, userId);
        if (rank == -1) {
            return "대기열에 없습니다.";
        }
        return "현재 대기 순번: " + rank + "등";
    }
}