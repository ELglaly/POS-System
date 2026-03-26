package org.example.cashier.services;

import org.example.cashier.core.entity.Transaction;
import org.example.cashier.core.entity.TransactionItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReceiptFormatter {

    private static final int WIDTH = 48;
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    @Value("${pos.shop.name:My Shop}")
    private String shopName;

    @Value("${pos.shop.address:123 Main Street}")
    private String shopAddress;

    @Value("${pos.receipt.footer:Thank you for your purchase!}")
    private String receiptFooter;

    @Value("${pos.currency.symbol: EGY }")
    private String currencySymbol;

    public List<String> format(Transaction tx) {
        List<String> lines = new ArrayList<>();

        lines.add(center(shopName));
        lines.add(center(shopAddress));
        lines.add(repeat('-', WIDTH));
        lines.add(leftRight("Date:", tx.getCreatedAt().format(DATE_FMT)));
        lines.add(leftRight("Receipt #:", String.valueOf(tx.getId())));
        if (tx.getCashier() != null) {
            lines.add(leftRight("Cashier:", tx.getCashier().getUsername()));
        }
        lines.add(repeat('=', WIDTH));
        lines.add(String.format("%-22s %4s %8s %8s", "Item", "Qty", "Unit", "Total"));
        lines.add(repeat('-', WIDTH));

        for (TransactionItem item : tx.getItems()) {
            lines.add(String.format("%-22s %4d %8.2f %8.2f",
                    truncate(item.getProductName(), 22),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()));
        }

        lines.add(repeat('-', WIDTH));
        lines.add(leftRight("Subtotal:", fmt(tx.getSubtotal())));

        if (tx.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            lines.add(leftRight("Discount:", "-" + fmt(tx.getDiscountAmount())));
        }

        lines.add(leftRight("TOTAL:", fmt(tx.getTotal())));
        lines.add(leftRight("Payment:", tx.getPaymentMethod().name()));

        if (tx.getPaymentMethod() == Transaction.PaymentMethod.CASH
                && tx.getAmountTendered() != null) {
            lines.add(leftRight("Tendered:", fmt(tx.getAmountTendered())));
            lines.add(leftRight("Change:",   fmt(tx.getChangeGiven())));
        }

        lines.add(repeat('=', WIDTH));
        lines.add(center(receiptFooter));
        lines.add("");

        return lines;
    }

    private String center(String text) {
        if (text == null || text.length() >= WIDTH) return text != null ? text : "";
        int pad = (WIDTH - text.length()) / 2;
        return " ".repeat(pad) + text;
    }

    private String leftRight(String left, String right) {
        int spaces = WIDTH - left.length() - right.length();
        if (spaces < 1) spaces = 1;
        return left + " ".repeat(spaces) + right;
    }

    private String repeat(char ch, int count) {
        return String.valueOf(ch).repeat(count);
    }

    private String truncate(String s, int max) {
        return (s == null) ? "" : (s.length() <= max ? s : s.substring(0, max - 1) + "...");
    }

    private String fmt(BigDecimal amount) {
        if (amount == null) return currencySymbol + "0.00";
        return currencySymbol +" "+ String.format("%.2f", amount);
    }
}
