package groom._55.repository;

import groom._55.entity.StampLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StampLogRepository extends JpaRepository<StampLog, Long> {

    List<StampLog> findTop5ByStamp_User_IdOrderByCreatedAtDesc(Long userId);
}
