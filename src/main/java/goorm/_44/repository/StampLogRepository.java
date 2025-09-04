package goorm._44.repository;

import goorm._44.entity.StampAction;
import goorm._44.entity.StampLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 특정 가게의 QR 스캔(방문) 횟수
    int countByStore_Id(Long storeId);

    // 특정 사용자가 특정 가게에서 남긴 특정 액션 횟수 (예: 쿠폰 사용 횟수)
    int countByStamp_User_IdAndStore_IdAndAction(Long userId, Long storeId, StampAction action);

    int countByStamp_User_IdAndStore_IdAndIdLessThanEqual(Long userId, Long storeId, Long logId);

    // 오늘 방문자 수 (distinct userId)
    @Query("SELECT COUNT(DISTINCT sl.stamp.user.id) " +
            "FROM StampLog sl " +
            "WHERE sl.store.id = :storeId " +
            "AND DATE(sl.createdAt) = CURRENT_DATE")
    int countTodayVisitors(Long storeId);

    // 오늘 신규 단골 수 (totalStamp == 1 && 오늘)
    @Query("SELECT COUNT(DISTINCT sl.stamp.user.id) " +
            "FROM StampLog sl " +
            "WHERE sl.store.id = :storeId " +
            "AND sl.stamp.totalStamp = 1 " +
            "AND DATE(sl.createdAt) = CURRENT_DATE")
    int countTodayNewRegulars(Long storeId);

    // 오늘 재방문 단골 수 (totalStamp > 1 && 오늘)
    @Query("SELECT COUNT(DISTINCT sl.stamp.user.id) " +
            "FROM StampLog sl " +
            "WHERE sl.store.id = :storeId " +
            "AND sl.stamp.totalStamp > 1 " +
            "AND DATE(sl.createdAt) = CURRENT_DATE")
    int countTodayRevisitRegulars(Long storeId);

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN l.action = 'REGISTER' THEN 1
                WHEN l.action = 'VISIT' THEN 1
                WHEN l.action = 'COUPON' THEN -10
                ELSE 0
            END
        ), 0)
        FROM StampLog l
        WHERE l.stamp.user.id = :userId
          AND l.store.id = :storeId
          AND l.createdAt <= :createdAt
    """)
    int calculateCumulative(
            @Param("userId") Long userId,
            @Param("storeId") Long storeId,
            @Param("createdAt") LocalDateTime createdAt
    );

}