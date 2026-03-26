package org.example.cashier.data.repository;

import org.example.cashier.core.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySkuAndActiveTrue(String sku);

    List<Product> findAllByActiveTrueOrderByName();

    @Query("""
           SELECT p FROM Product p
           WHERE p.active = true
           AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(p.sku)  LIKE LOWER(CONCAT('%', :q, '%')))
           ORDER BY p.name
           """)
    List<Product> searchActive(@Param("q") String query);
}
