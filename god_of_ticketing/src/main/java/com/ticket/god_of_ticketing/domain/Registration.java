package com.ticket.god_of_ticketing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 학생 ID (로그인 기능이 없으니 숫자로 대체)

    private Long lectureId; // 강의 ID

    public Registration() {
    }
    
    public Registration(Long userId, Long lectureId) {
        this.userId = userId;
        this.lectureId = lectureId;
    }
}