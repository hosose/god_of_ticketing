package com.ticket.god_of_ticketing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * jpa 방식
 * 1. 자바 객체만 신경 쓴다.
 * 2. jpa가 자바 클래스를 보고 "이런 모양의 테이블이 필요하겠군" 하고 알아서 테이블 생성
 * 3. 데이터를 저장할 때도 자바 객체를 던져주면, JPA가 알아서 SQL을 만들어서 DB에 날립니다.
 * 
 * "이 코드는 자바 클래스처럼 보이지만, 사실은 '살아서 움직이는 DB 테이블 설계도'입니다."
 */

/*
 * MyBatis와의 결정적 차이:
 * MyBatis: 비즈니스 로직이 SQL에 들어갑니다. (UPDATE lecture SET current = current + 1 WHERE id = ?)
 * JPA: 비즈니스 로직이 자바 객체 안에 있습니다.
 * 우리는 그냥 자바 객체의 값만 ++ 시켰을 뿐인데, 나중에 repository.save()를 하면 JPA가 "어? 값이 변했네?" 하고 알아서 Update 쿼리를 날립니다. 이것을 객체 지향적인 개발이라고 합니다.
 */

@Entity  // 데이터베이스 테이블 인식
@Getter // 이게 있어도 인식이 안 되면 아래 코드를 직접 넣어야 합니다.
@NoArgsConstructor
public class Lecture {

    @Id // 이 필드가 이 테이블의 PK야
    @GeneratedValue(strategy = GenerationType.IDENTITY) //PK 값 알아서 auto increment 하는 방식
    private Long id;

    private String title;

    private int maxCapacity; //java의 camelCase는 DB의 snake_case로 자동 변환

    private int currentCapacity;

    //이 코드가 JPA의 독특한 특징
    /*
     * JPA는 DB에서 데이터를 가져와서 자바 객체로 만들 때, **리플렉션(Reflection)**이라는 기술을 씁니다.
     * 쉽게 비유하자면, JPA는 **"빈 깡통 객체(new Lecture())"**를 먼저 하나 딱 만들어놓고, 그 다음에 DB 값을 하나씩 밀어 넣는 방식을 씁니다.
     * 그래서 **매개변수가 없는 깡통 생성자(기본 생성자)**가 없으면, JPA가 "어? 빈 깡통을 못 만들겠는데?" 하고 아까 보신 InstantiationException 에러를 뱉는 겁니다.
     */
    public Lecture() {
    }
    
    public Lecture(String title, int maxCapacity) {
        this.title = title;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = 0;
    }

    public void increaseCapacity() {
        this.currentCapacity++;
    }

    // ========== [여기 아래 코드를 추가해 주세요] ==========

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
    
    public Long getId() { // 혹시 모르니 ID도 추가
        return id;
    }
}