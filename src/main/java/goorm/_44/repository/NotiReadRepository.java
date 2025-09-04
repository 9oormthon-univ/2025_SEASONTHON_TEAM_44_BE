package goorm._44.repository;

import goorm._44.entity.NotiRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotiReadRepository extends JpaRepository<NotiRead, Long> {
    boolean existsByUserIdAndNotiId(Long userId, Long notiId);
}
