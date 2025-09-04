package goorm._44.repository;

import goorm._44.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByUserId(Long userId);

    @Modifying
    @Query("update Store s set s.imageKey = :imageKey where s.id = :id")
    int updateImageKey(Long id, String imageKey);

    boolean existsByUserId(Long userId);

    int countByUserId(Long userId);
}
