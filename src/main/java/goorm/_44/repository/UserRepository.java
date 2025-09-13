package goorm._44.repository;

import goorm._44.enums.Role;
import goorm._44.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String username);
    Optional<User> findByPassword(String password);

    Optional<User> findByPasswordAndRole(String password, Role role);

}
