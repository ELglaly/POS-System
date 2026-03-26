package org.example.cashier.services;

import org.example.cashier.core.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Sales reporting and analytics queries.
 */
public interface ReportService {

    /** Total revenue for a single calendar day. */
    BigDecimal getDailyTotal(LocalDate date);

    /** All transactions (with cashier fetched) in the given inclusive date range. */
    List<Transaction> getHistory(LocalDate from, LocalDate to);

    /** All-time top-N products by quantity sold: [productName, totalQty]. */
    List<Object[]> getTopProducts(int limit);

    /** Aggregated KPI summary for the given date range. */
    RangeSummary getRangeSummary(LocalDate from, LocalDate to);

    /**
     * Top-N products by revenue in the given date range.
     * Each row: [productName (String), qty (Long), revenue (BigDecimal)].
     */
    List<Object[]> getTopProductsByRevenue(int limit, LocalDate from, LocalDate to);

    /**
     * Payment method breakdown for the given date range.
     * Each row: [paymentMethod (String), count (Long), total (BigDecimal)].
     */
    List<Object[]> getPaymentBreakdown(LocalDate from, LocalDate to);

    // ── Value object ──────────────────────────────────────────────────────

    record RangeSummary(
            BigDecimal totalRevenue,
            long       txCount,
            BigDecimal avgOrderValue,
            long       itemsSold,
            BigDecimal totalDiscount
    ) {}
}
