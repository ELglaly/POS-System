package org.example.cashier.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cashier.core.entity.Transaction;
import org.example.cashier.services.ReceiptFormatter;
import org.example.cashier.services.ReceiptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptServiceImpl implements ReceiptService {

    @Value("${pos.printer.name:}")
    private String printerName;

    private final ReceiptFormatter formatter;

    @Override
    public void printReceipt(Transaction tx) {
        List<String> lines = formatter.format(tx);
        String text = String.join("\n", lines);

        PrintService printer = resolvePrinter(printerName);
        if (printer == null) {
            log.warn("No printer available — skipping receipt print.");
            return;
        }

        try {
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(text.getBytes(), flavor, null);
            PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            attrs.add(new Copies(1));
            printer.createPrintJob().print(doc, attrs);
        } catch (PrintException e) {
            throw new RuntimeException("Print failed: " + e.getMessage(), e);
        }
    }

    private PrintService resolvePrinter(String name) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (name == null || name.isBlank()) {
            return PrintServiceLookup.lookupDefaultPrintService();
        }
        for (PrintService ps : services) {
            if (ps.getName().equalsIgnoreCase(name)) return ps;
        }
        return PrintServiceLookup.lookupDefaultPrintService();
    }
}
