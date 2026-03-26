package org.example.cashier.data.repository;

import org.example.cashier.core.entity.PrintJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrintJobRepository extends JpaRepository<PrintJob, Long> {

    /** Print history for the barcode screen history table — most recent first. */
    List<PrintJob> findTop50ByOrderByCreatedAtDesc();

    /** Per-product history for the side detail panel. */
    List<PrintJob> findByProductIdOrderByCreatedAtDesc(Long productId);

    /** Jobs by operator (cashier performance / audit). */
    List<PrintJob> findByOperatorUsernameOrderByCreatedAtDesc(String username);

    @Query("""
           SELECT j FROM PrintJob j
           WHERE j.createdAt BETWEEN :from AND :to
           ORDER BY j.createdAt DESC
           """)
    List<PrintJob> findByDateRange(@Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    /** Count completed jobs for dashboard KPI. */
    long countByStatus(PrintJob.PrintJobStatus status);
}
