package com.ticket.god_of_ticketing.service;

import java.util.Collection;

import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

	private final RedissonClient redissonClient;

	public QueueService(RedissonClient redissonClient) {
		this.redissonClient = redissonClient;
	}

	// 1. 대기열 등록 (번호표 뽑기)
	public void addQueue(Long lectureId, Long userId) {
		// ZSET 자료구조 가져오기
		RScoredSortedSet<Long> queue = redissonClient.getScoredSortedSet("lecture_queue:" + lectureId);

		// 현재 시간(도착 시간)을 점수로 해서 저장
		// add(점수, 값) -> 이미 있으면 무시됨
		queue.add(System.currentTimeMillis(), userId);
	}

	// 2. 내 순서 확인 (몇 번째인지 조회)
	// 0등부터 시작하므로 +1 해서 리턴
	public Long getRank(Long lectureId, Long userId) {
		RScoredSortedSet<Long> queue = redissonClient.getScoredSortedSet("lecture_queue:" + lectureId);

		// rank: 내 등수 (0부터 시작, 없으면 null)
		Integer rank = queue.rank(userId);

		if (rank == null) {
			return -1L; // 대기열에 없음
		}
		return rank + 1L; // 1등, 2등... 으로 표현
	}

	// ✅ [수정된 부분] 3. 상위 N명 조회
    // readSort() -> valueRange()로 변경 (ZRANGE 명령어 사용)
    // 리턴 타입도 Set -> Collection으로 변경 (Redisson API 스펙)
    public Collection<Long> getFirstTokens(Long lectureId, long count) {
        RScoredSortedSet<Long> queue = redissonClient.getScoredSortedSet("lecture_queue:" + lectureId);
        // 0번(1등)부터 count-1 번까지 조회
        return queue.valueRange(0, (int) count - 1);
    }

	// 4. 입장 처리 후 대기열에서 삭제 (Pop)
	public void removeQueue(Long lectureId, Long userId) {
		RScoredSortedSet<Long> queue = redissonClient.getScoredSortedSet("lecture_queue:" + lectureId);
		queue.remove(userId);
	}
}