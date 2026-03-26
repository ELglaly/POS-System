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
    List<PrintJob> findTop50ByOrderByCreatedAtDesc();
    List<PrintJob> findByProductIdOrderByCreatedAtDesc(Long productId);
}
