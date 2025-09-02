package groom._55.repository;

import groom._55.entity.Noti;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotiRepository extends JpaRepository<Noti, Long> {
    Noti findByStoreId(Long storeId);
}
