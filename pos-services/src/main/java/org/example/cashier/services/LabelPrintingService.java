package org.example.cashier.services;

import org.example.cashier.core.dto.BarcodeGenerationRequestDTO;
import org.example.cashier.core.dto.PrintJobDTO;
import org.example.cashier.core.enums.LabelTemplate;

import java.nio.file.Path;
import java.util.List;

/**
 * Contract for label printing and PDF export.
 * Implementations must persist a {@code PrintJob} record and publish
 * {@code PrintJobCompletedEvent} regardless of success or failure.
 */
public interface LabelPrintingService {

    /**
     * Submit a print job to the named printer (or system default if null/blank).
     * Runs asynchronously via {@code @Async} — returns the created job record.
     */
    PrintJobDTO print(BarcodeGenerationRequestDTO request, String printerName);

    /**
     * Export labels as a PDF file to the given output path.
     * Supports batch: creates a page per copy.
     */
    Path exportPdf(BarcodeGenerationRequestDTO request, Path outputPath);

    /** Retrieve print history — most recent 50 jobs for the history table. */
    List<PrintJobDTO> getRecentHistory();

    /** Retrieve print history for a specific product. */
    List<PrintJobDTO> getHistoryForProduct(Long productId);

    /** List all available printer names on this machine. */
    List<String> getAvailablePrinters();
}
