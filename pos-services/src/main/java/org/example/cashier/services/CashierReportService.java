package org.example.cashier.services;

import lombok.RequiredArgsConstructor;
import org.example.cashier.core.entity.Transaction;
import org.example.cashier.data.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CashierReportService implements ReportService {

    private final TransactionRepository transactionRepository;

    // ── Basic ──────────────────────────────────────────────────────────────

    @Override
    public BigDecimal getDailyTotal(LocalDate date) {
        BigDecimal result = transactionRepository.getDailyTotal(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public List<Transaction> getHistory(LocalDate from, LocalDate to) {
        return transactionRepository.findByDateRange(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay());
    }

    @Override
    public List<Object[]> getTopProducts(int limit) {
        return transactionRepository.findTopProducts(PageRequest.of(0, limit));
    }

    // ── Analytics ──────────────────────────────────────────────────────────

    @Override
    public RangeSummary getRangeSummary(LocalDate from, LocalDate to) {
        var start = from.atStartOfDay();
        var end   = to.plusDays(1).atStartOfDay();

        BigDecimal revenue  = transactionRepository.getDailyTotal(start, end);
        if (revenue == null) revenue = BigDecimal.ZERO;

        BigDecimal discount = transactionRepository.getTotalDiscount(start, end);
        if (discount == null) discount = BigDecimal.ZERO;

        long txCount   = transactionRepository.countCompleted(start, end);
        long itemsSold = transactionRepository.getTotalItemsSold(start, end);

        BigDecimal avg = txCount > 0
                ? revenue.divide(BigDecimal.valueOf(txCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new RangeSummary(revenue, txCount, avg, itemsSold, discount);
    }

    @Override
    public List<Object[]> getTopProductsByRevenue(int limit, LocalDate from, LocalDate to) {
        return transactionRepository.findTopProductsByRevenue(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay(),
                PageRequest.of(0, limit));
    }

    @Override
    public List<Object[]> getPaymentBreakdown(LocalDate from, LocalDate to) {
        return transactionRepository.getPaymentBreakdown(
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay());
    }

}
