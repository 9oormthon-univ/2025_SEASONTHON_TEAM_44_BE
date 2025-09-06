package goorm._44.repository;

import goorm._44.entity.Noti;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NotiRepository extends JpaRepository<Noti, Long> {
    List<Noti> findByStoreId(Long storeId);
    List<Noti> findByStoreIdOrderByCreatedAtDesc(Long storeId);
    Optional<Noti> findFirstByStoreIdOrderByCreatedAtDesc(Long storeId);

    // 페이지네이션
    Page<Noti> findByStoreId(Long storeId, Pageable pageable);

    // 페이지 내 공지들에 대한 읽음 수 일괄 집계
    @Query("""
        SELECT nr.noti.id, COUNT(nr)
        FROM NotiRead nr
        WHERE nr.noti.id IN :ids
        GROUP BY nr.noti.id
    """)
    List<Object[]> countReadsByNotiIds(Collection<Long> ids);
}
