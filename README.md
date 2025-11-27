# 🚀 선착순의 신 (God of Ticketing)
> **대용량 트래픽 상황에서의 동시성 제어(Concurrency Control) 및 데이터 정합성 보장 프로젝트**

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-green)
![JPA](https://img.shields.io/badge/JPA-Hibernate-lightgrey)
![Redis](https://img.shields.io/badge/Redis-Distributed_Lock-red)
![Docker](https://img.shields.io/badge/Docker-Container-blue)

---

## 📖 프로젝트 개요
대학 수강신청이나 콘서트 예매처럼 **한정된 수량(재고)에 수많은 트래픽이 동시에 몰리는 상황**을 시뮬레이션한 프로젝트입니다.
기존 RDBMS 로직의 한계인 **Race Condition(경쟁 상태)** 문제를 해결하기 위해 **Redis 분산 락(Distributed Lock)**을 도입하여 데이터 무결성을 100% 보장하는 것을 목표로 했습니다.

---

## 💥 핵심 문제 및 해결 (Key Challenges)

### 1. 문제 상황 (Race Condition)
* **시나리오:** 수강 정원이 **30명**인 강의에 **100명**의 유저가 동시에 신청 요청을 보냄.
* **현상:** `Current < Max` 조건을 통과한 스레드가 동시에 DB에 접근하여, 최종적으로 **70명 이상**이 신청되는 초과 수강 사고 발생.
* **원인:** 조회(Read)와 수정(Write) 사이의 원자성(Atomicity)이 보장되지 않음.

### 2. 해결 전략 (Redis Distributed Lock)
* **Redisson 라이브러리**를 도입하여 분산 락 구현.
* **Pub/Sub 방식**의 락을 사용하여 스핀 락(Spin Lock) 대비 Redis 부하를 최소화.
* **로직:**
  1. 요청 시 Redis에서 `Key(Lock)` 획득 시도.
  2. 획득에 성공한 **단 1개의 스레드**만 DB 트랜잭션 진입.
  3. 로직 수행 후 락 해제 (Unlock).

---

## 🛠 기술 스택 (Tech Stack)

* **Backend:** Java 17, Spring Boot 3.3.5
* **Database:** H2 (In-Memory Mode)
* **Persistence:** Spring Data JPA
* **Concurrency Control:** Redis (Docker), Redisson 3.34.1
* **Test:** JUnit 5 (Multi-thread Test)

---

## 🏗 시스템 아키텍처 (Architecture)

```mermaid
graph LR
    A["User Traffic (100 Request)"] --> B{Redis Lock}
    B -- Lock Acquired --> C[Transaction Start]
    C --> D{Check Capacity}
    D -- Available --> E["Save & Count Up"]
    E --> F["Commit & Unlock"]
    B -- Lock Failed --> G[Wait or Fail]
