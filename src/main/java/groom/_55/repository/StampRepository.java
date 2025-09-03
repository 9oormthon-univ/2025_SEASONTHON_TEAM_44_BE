package groom._55.repository;

import groom._55.entity.Stamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StampRepository extends JpaRepository<Stamp, Long> {
    Optional<Stamp> findByUserIdAndStoreId(Long userId, Long storeId);
}
