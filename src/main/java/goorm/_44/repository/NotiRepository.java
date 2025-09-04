package goorm._44.repository;

import goorm._44.entity.Noti;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotiRepository extends JpaRepository<Noti, Long> {
    List<Noti> findByStoreId(Long storeId);
    Optional<Noti> findFirstByStoreIdOrderByCreatedAtDesc(Long storeId);
}
