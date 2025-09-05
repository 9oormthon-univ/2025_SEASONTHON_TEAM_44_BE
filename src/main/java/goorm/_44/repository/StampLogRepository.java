package goorm._44.repository;

import goorm._44.entity.StampAction;
import goorm._44.entity.StampLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StampLogRepository extends JpaRepository<StampLog, Long> {

    List<StampLog> findTop20ByStamp_User_IdOrderByCreatedAtDesc(Long userId);

    // 사장님 가게의 모든 로그 최신순
    List<StampLog> findByStore_IdOrderByCreatedAtDesc(Long storeId);

    // 특정 사용자가 특정 가게에서 남긴 특정 액션 횟수 (예: 쿠폰 사용 횟수)
    int countByStamp_User_IdAndStore_IdAndAction(Long userId, Long storeId, StampAction action);

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

//    @Query("SELECT COUNT(sl) FROM StampLog sl " +
//            "WHERE sl.store.id = :storeId " +
//            "AND DATE(sl.createdAt) = :date " +
//            "AND sl.action IN :actions")
//    int countByStoreAndDateAndActions(@Param("storeId") Long storeId,
//                                      @Param("date") LocalDate date,
//                                      @Param("actions") List<StampAction> actions);
//
//    @Query("SELECT COUNT(sl) FROM StampLog sl " +
//            "WHERE sl.store.id\n = :storeId " +
//            "AND DATE(sl.createdAt) = :date " +
//            "AND sl.action = :action")
//    int countByStoreAndDateAndAction(@Param("storeId") Long storeId,
//                                     @Param("date") LocalDate date,
//                                     @Param("action") StampAction action);


    // ✅ 특정 가게, 특정 날짜, 여러 액션별 고유 유저 수
    @Query("""
        SELECT COUNT(DISTINCT sl.stamp.user.id)
        FROM StampLog sl
        WHERE sl.store.id = :storeId
          AND DATE(sl.createdAt) = :date
          AND sl.action IN :actions
    """)
    int countDistinctUsersByStoreAndDateAndActions(@Param("storeId") Long storeId,
                                                   @Param("date") LocalDate date,
                                                   @Param("actions") List<StampAction> actions);

    // ✅ 특정 가게, 특정 날짜, 단일 액션별 고유 유저 수
    @Query("""
        SELECT COUNT(DISTINCT sl.stamp.user.id)
        FROM StampLog sl
        WHERE sl.store.id = :storeId
          AND DATE(sl.createdAt) = :date
          AND sl.action = :action
    """)
    int countDistinctUsersByStoreAndDateAndAction(@Param("storeId") Long storeId,
                                                  @Param("date") LocalDate date,
                                                  @Param("action") StampAction action);
}