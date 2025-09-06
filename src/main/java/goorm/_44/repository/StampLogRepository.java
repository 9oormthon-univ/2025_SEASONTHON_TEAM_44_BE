package goorm._44.repository;

import goorm._44.entity.StampAction;
import goorm._44.entity.StampLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 기존 List -> Page 로 교체 (정렬은 Pageable에서 지정)
    Page<StampLog> findByStore_Id(Long storeId, Pageable pageable);

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

    // ※ JPQL에서 DATE() 함수는 구현체 의존적이라, 필요시 function('date', sl.createdAt) 형태로 바꿔도 됨.
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
