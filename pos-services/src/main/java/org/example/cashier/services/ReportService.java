package org.example.cashier.services;

import org.example.cashier.core.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    BigDecimal getDailyTotal(LocalDate date);

    List<Transaction> getHistory(LocalDate from, LocalDate to);

    List<Object[]> getTopProducts(int limit);

    RangeSummary getRangeSummary(LocalDate from, LocalDate to);

    List<Object[]> getTopProductsByRevenue(int limit, LocalDate from, LocalDate to);

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
