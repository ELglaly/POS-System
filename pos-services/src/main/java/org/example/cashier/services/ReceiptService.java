package org.example.cashier.services;

import org.example.cashier.core.entity.Transaction;

public interface ReceiptService {
    void printReceipt(Transaction tx);
}
