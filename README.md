# 🚀 선착순의 신 (God of Ticketing)
> **대용량 트래픽 상황에서의 동시성 제어 및 대기열 시스템(Traffic Throttling) 구축 프로젝트**

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-green)
![JPA](https://img.shields.io/badge/JPA-Hibernate-lightgrey)
![Redis](https://img.shields.io/badge/Redis-Distributed_Lock_%26_ZSet-red)
![Docker](https://img.shields.io/badge/Docker-Container-blue)

---

## 📖 프로젝트 개요
대학 수강신청이나 콘서트 예매처럼 **한정된 수량에 수많은 트래픽이 동시에 몰리는 상황**을 시뮬레이션한 프로젝트입니다.
초기에는 **Redis 분산 락**을 통해 데이터 정합성을 확보했고, 이후 **Redis 대기열(Queue)과 스케줄러**를 도입하여 DB 부하를 제어하고 사용자 경험(UX)을 개선하는 아키텍처로 고도화하였습니다.

---

## 📅 개발 단계 (Evolution)

### Phase 1. 데이터 정합성 보장 (Concurrency Control)
* **문제:** 100명이 동시에 신청 시 30명 정원을 초과하는 **Race Condition** 발생.
* **해결:** **Redis 분산 락(Redisson)** 도입.
* **결과:** 동시 요청 시에도 정확히 30명만 성공하도록 데이터 무결성 보장.

### Phase 2. 대기열 시스템 구축 (Queue & Throttling)
* **문제:** 분산 락만으로는 사용자가 무한정 대기하거나 튕겨 나가는 경험(UX) 발생. 또한 트래픽 폭주 시 DB 커넥션 고갈 위험.
* **해결:**
    1. **Redis ZSET(Sorted Set):** 요청 순서대로 대기표를 발급하는 인메모리 대기열 구현.
    2. **스케줄러(Scheduler):** 서버가 처리 가능한 양만큼(예: 10명/초)만 입장시키는 **Traffic Throttling** 구현.
    3. **Polling:** 클라이언트가 자신의 대기 순번을 실시간으로 확인할 수 있는 API 구현.
* **결과:** 시스템 안정성 확보 및 대기 순번 시각화 제공.

---

## 💥 핵심 기술적 챌린지 (Key Challenges)

### 1. Redis 분산 락 (Distributed Lock)
* **구현:** Redisson의 `tryLock`을 사용하여 스핀 락(Spin Lock) 대비 Redis 부하를 줄이는 Pub/Sub 방식 적용.
* **최적화:** 로직 수행 중 락이 해제되는 사고를 방지하기 위해 `leaseTime`을 충분히 확보(10s)하고, 대기 시간(`waitTime`)을 설정하여 데드락 방지.

### 2. 순서 보장 대기열 (Redis ZSET)
* **구조:** `Key: lecture_queue`, `Value: userId`, `Score: System.currentTimeMillis()`
* **이유:** 단순 `List`보다 `Sorted Set`을 사용하여 시간 순서대로 정렬하고, `Rank` 조회 성능을 $O(\log N)$으로 최적화함.

### 3. 유량 제어 (Traffic Throttling)
* **전략:** 스프링 스케줄러(`@Scheduled`)를 이용해 1초마다 대기열 상위 N명을 `Pop`하여 비즈니스 로직으로 넘김.
* **가변 주기(Adaptive Polling):** 대기열이 비어있을 땐 3초 주기, 트래픽이 감지되면 0.1초 주기로 자동 전환하여 **반응 속도와 리소스 효율성**을 동시에 잡음 (`SchedulingConfigurer` 구현).

---

## 🏗 시스템 아키텍처 (Architecture)

### 최종 흐름도 (Queue + Scheduler)

```mermaid
sequenceDiagram
    participant User
    participant Controller
    participant Redis(ZSET)
    participant Scheduler
    participant Service(Lock)
    participant DB

    User->>Controller: 1. 수강신청 요청 (Join Queue)
    Controller->>Redis(ZSET): 2. 대기열 등록 (Score=Time)
    Controller-->>User: "대기열 등록 완료"

    loop Every 100ms (Adaptive)
        Scheduler->>Redis(ZSET): 3. 상위 10명 조회 (ZRANGE)
        Scheduler->>Service(Lock): 4. 수강신청 로직 실행
        Service(Lock)->>DB: 5. 재고 확인 및 감소
        Scheduler->>Redis(ZSET): 6. 처리 완료 유저 삭제 (ZREM)
    end

    loop Every 1 sec
        User->>Controller: 7. 내 순서 확인 (Polling)
        Controller->>Redis(ZSET): 8. 순위 조회 (ZRANK)
        Controller-->>User: "현재 5등입니다"
    end
