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

    /** Case-insensitive name/SKU search for the product search bar. */
    @Query("""
           SELECT p FROM Product p
           WHERE p.active = true
           AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(p.sku)  LIKE LOWER(CONCAT('%', :q, '%')))
           ORDER BY p.name
           """)
    List<Product> searchActive(@Param("q") String query);

    /** Products with stock at or below their low-stock threshold. */
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity <= p.lowStockThreshold ORDER BY p.stockQuantity")
    List<Product> findLowStock();

    /** Products with zero stock. */
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity = 0")
    List<Product> findOutOfStock();

    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    /** Count of active products (used in dashboard KPI). */
    long countByActiveTrue();
}
