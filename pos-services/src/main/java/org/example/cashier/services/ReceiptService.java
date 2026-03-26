package org.example.cashier.services;

import org.example.cashier.core.entity.Transaction;

/**
 * Sends a formatted receipt to the configured printer.
 * If no printer is available the call is a no-op (logged as warning).
 */
public interface ReceiptService {

    void printReceipt(Transaction tx);
}
