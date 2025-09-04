package goorm._44.repository;

import goorm._44.entity.Stamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StampRepository extends JpaRepository<Stamp, Long> {
    Optional<Stamp> findByUserIdAndStoreId(Long userId, Long storeId);
    List<Stamp> findByUserId(Long userId);
    Optional<Stamp> findByIdAndUserId(Long stampId, Long userId);

    // 해당 매장의 단골 전체 수 (Stamp 한 줄 = 한 사용자-매장 관계라고 가정)
    int countByStoreId(Long storeId);
    // 인증 단골: totalStamp >= 10
    int countByStoreIdAndTotalStampGreaterThanEqual(Long storeId, int totalStamp);
    // 일반 단골: totalStamp < 10
    int countByStoreIdAndTotalStampLessThan(Long storeId, int totalStamp);

    // 특정 유저의 해당 매장 totalStamp 조회 (없으면 0 반환)
    @Query("SELECT COALESCE(s.totalStamp, 0) FROM Stamp s WHERE s.user.id = :userId AND s.store.id = :storeId")
    int findTotalStampByUserAndStore(@Param("userId") Long userId, @Param("storeId") Long storeId);
}
