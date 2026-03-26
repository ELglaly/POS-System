package org.example.cashier.data.repository;

import org.example.cashier.core.entity.Barcode;
import org.example.cashier.core.enums.BarcodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarcodeRepository extends JpaRepository<Barcode, Long> {

    List<Barcode> findByProductIdAndActiveTrueOrderByGeneratedAtDesc(Long productId);
    @Modifying
    @Query("""
           UPDATE Barcode b SET b.active = false
           WHERE b.product.id = :productId AND b.barcodeType = :type AND b.active = true
           """)
    int deactivateByProductIdAndType(@Param("productId") Long productId,
                                     @Param("type") BarcodeType type);

    boolean existsByBarcodeValue(String barcodeValue);
}
