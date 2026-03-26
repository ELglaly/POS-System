package org.example.cashier.data.repository;

import org.example.cashier.core.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
           SELECT t FROM Transaction t LEFT JOIN FETCH t.cashier
           WHERE t.createdAt >= :start AND t.createdAt < :end
           ORDER BY t.createdAt DESC
           """)
    List<Transaction> findByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end);

    @Query("""
           SELECT COALESCE(SUM(t.total), 0) FROM Transaction t
           WHERE t.createdAt >= :start AND t.createdAt < :end
           AND t.status = org.example.cashier.core.entity.Transaction$TransactionStatus.COMPLETED
           """)
    BigDecimal getDailyTotal(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end);

    @Query("""
           SELECT COALESCE(SUM(t.discountAmount), 0) FROM Transaction t
           WHERE t.createdAt >= :start AND t.createdAt < :end
           AND t.status = org.example.cashier.core.entity.Transaction$TransactionStatus.COMPLETED
           """)
    BigDecimal getTotalDiscount(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end);

    @Query("""
           SELECT COUNT(t) FROM Transaction t
           WHERE t.createdAt >= :start AND t.createdAt < :end
           AND t.status = org.example.cashier.core.entity.Transaction$TransactionStatus.COMPLETED
           """)
    long countCompleted(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end);

    @Query("""
           SELECT COALESCE(SUM(ti.quantity), 0)
           FROM TransactionItem ti
           WHERE ti.transaction.createdAt >= :start AND ti.transaction.createdAt < :end
           AND ti.transaction.status = org.example.cashier.core.entity.Transaction$TransactionStatus.COMPLETED
           """)
    long getTotalItemsSold(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end);

    /** Returns [paymentMethod (String), count (Long), total (BigDecimal)] per method. */
    @Query("""
           SELECT t.paymentMethod, COUNT(t), COALESCE(SUM(t.total), 0)
           FROM Transaction t
           WHERE t.createdAt >= :start AND t.createdAt < :end
           AND t.status = org.example.cashier.core.entity.Transaction$TransactionStatus.COMPLETED
           GROUP BY t.paymentMethod
           """)
    List<Object[]> getPaymentBreakdown(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end);

    /** Returns [productName (String), qty (Long), revenue (BigDecimal)] in date range. */
    @Query("""
           SELECT ti.productName, SUM(ti.quantity), SUM(ti.subtotal)
           FROM TransactionItem ti
           WHERE ti.transaction.createdAt >= :start AND ti.transaction.createdAt < :end
           AND ti.transaction.status = org.example.cashier.core.entity.Transaction$TransactionStatus.COMPLETED
           GROUP BY ti.productName
           ORDER BY SUM(ti.subtotal) DESC
           """)
    List<Object[]> findTopProductsByRevenue(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end,
            Pageable pageable);

    /** All-time top products by qty sold (original query, kept for fallback). */
    @Query("""
           SELECT ti.productName, SUM(ti.quantity) AS total
           FROM TransactionItem ti
           GROUP BY ti.productName
           ORDER BY total DESC
           """)
    List<Object[]> findTopProducts(Pageable pageable);
}
