package org.example.cashier.services;

import org.example.cashier.core.dto.BarcodeGenerationRequestDTO;
import org.example.cashier.core.dto.PrintJobDTO;
import java.nio.file.Path;
import java.util.List;

/**
 * Contract for label printing and PDF export.
 * Implementations must persist a {@code PrintJob} record and publish
 * {@code PrintJobCompletedEvent} regardless of success or failure.
 */
public interface LabelPrintingService {

    PrintJobDTO print(BarcodeGenerationRequestDTO request, String printerName);

    Path exportPdf(BarcodeGenerationRequestDTO request, Path outputPath);

    List<PrintJobDTO> getRecentHistory();

    List<PrintJobDTO> getHistoryForProduct(Long productId);

    List<String> getAvailablePrinters();
}
