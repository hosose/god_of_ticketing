package com.ticket.god_of_ticketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ticket.god_of_ticketing.domain.Lecture;
/*
 ① extends JpaRepository (상속의 마법)
의미: "스프링아, 나한테 기본적인 CRUD(저장, 조회, 수정, 삭제) 기능을 다 물려줘."

이 한 줄 덕분에, save(), delete(), findById() 같은 메서드를 직접 만들지 않아도 부모(JpaRepository)한테 물려받아서 바로 쓸 수 있는 겁니다.

② <Lecture, Long> (제네릭: 타겟 설정)
이 꺽쇠 < > 안에 적는 것이 "누구를", "무엇으로" 다룰지 정하는 설정값입니다.

Lecture: "이 리모컨은 Lecture 테이블(Entity) 전용이야."

Long: "그리고 Lecture 테이블의 **PK(ID)**는 **Long 타입(숫자)**이야."

요약: "Lecture 테이블을 조작할 건데, PK는 Long 타입이야. 기본적인 SQL 기능은 다 줘."
 */
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    // 텅 비어있어도 findById, save 같은 메서드를 다 쓸 수 있습니다.
}