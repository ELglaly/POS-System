package org.example.cashier.data.repository;

import org.example.cashier.core.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    Optional<Category> findByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);
}
