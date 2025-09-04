package goorm._44.repository;

import goorm._44.entity.StampLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StampLogRepository extends JpaRepository<StampLog, Long> {

    List<StampLog> findTop5ByStamp_User_IdOrderByCreatedAtDesc(Long userId);

    // 사장님 가게의 모든 로그 최신순
    List<StampLog> findByStore_IdOrderByCreatedAtDesc(Long storeId);

    // 특정 고객이 해당 가게에서 '해당 시점까지' 찍은 로그 수 (누적)
    int countByStamp_User_IdAndStore_IdAndCreatedAtLessThanEqual(
            Long userId, Long storeId, LocalDateTime createdAt
    );
}