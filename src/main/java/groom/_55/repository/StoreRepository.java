package groom._55.repository;

import groom._55.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByUserId(Long userId);
}
