package groom._55.repository;

import groom._55.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByName(String username);
    Optional<User> findByRole(String role);

}
