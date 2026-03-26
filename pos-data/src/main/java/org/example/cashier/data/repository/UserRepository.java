package org.example.cashier.data.repository;

import org.example.cashier.core.entity.User;
import org.example.cashier.core.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndActiveTrue(String username);

    Optional<User> findByBarcodePinAndActiveTrue(String barcodePin);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findAllByActiveTrueOrderByUsername();

    long countByActiveTrue();
}
