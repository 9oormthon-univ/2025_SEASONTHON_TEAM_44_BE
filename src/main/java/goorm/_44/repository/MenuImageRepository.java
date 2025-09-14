package goorm._44.repository;

import goorm._44.entity.MenuImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuImageRepository extends JpaRepository<MenuImage, Long> {
    List<MenuImage> findByStoreId(Long storeId);
}
