package org.example.cashier.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cashier.core.dto.BarcodeGenerationRequestDTO;
import org.example.cashier.core.dto.PrintJobDTO;
import org.example.cashier.core.entity.PrintJob;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.exception.PrintJobException;
import org.example.cashier.core.exception.ProductNotFoundException;
import org.example.cashier.core.mapper.PrintJobMapper;
import org.example.cashier.data.repository.PrintJobRepository;
import org.example.cashier.data.repository.ProductRepository;
import org.example.cashier.reports.barcode.EscPosPrinterAdapter;
import org.example.cashier.reports.barcode.PdfLabelExporter;
import org.example.cashier.services.BarcodeGenerationService;
import org.example.cashier.services.LabelPrintingService;
import org.example.cashier.services.event.PrintJobCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.PrintServiceLookup;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabelPrintingServiceImpl implements LabelPrintingService {

    private final PrintJobRepository        printJobRepository;
    private final ProductRepository         productRepository;
    private final BarcodeGenerationService  barcodeGenerationService;
    private final EscPosPrinterAdapter      escPosPrinter;
    private final PdfLabelExporter          pdfExporter;
    private final PrintJobMapper            printJobMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Async("printTaskExecutor")
    @Transactional
    public PrintJobDTO print(BarcodeGenerationRequestDTO request, String printerName) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        PrintJob job = PrintJob.builder()
                .product(product)
                .barcodeType(request.getBarcodeType())
                .labelTemplate(request.getLabelTemplate())
                .copies(request.getCopies())
                .printerName(printerName)
                .operatorUsername(request.getOperatorUsername())
                .status(PrintJob.PrintJobStatus.PRINTING)
                .build();
        job = printJobRepository.save(job);

        try {
            // Generate the barcode image bytes
            byte[] imagePng = barcodeGenerationService.renderImagePng(
                    barcodeGenerationService.findActiveByProduct(product.getId())
                            .stream()
                            .filter(b -> b.getBarcodeType() == request.getBarcodeType())
                            .findFirst()
                            .map(b -> b.getBarcodeValue())
                            .orElseGet(() -> barcodeGenerationService
                                    .generate(request).getBarcodeValue()),
                    request.getBarcodeType(),
                    400, 150);

            escPosPrinter.printLabel(imagePng, product, request.getLabelTemplate(),
                    request.getCopies(), printerName);

            job.setStatus(PrintJob.PrintJobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            log.info("Print job {} completed: {} x {} copies on {}",
                    job.getId(), product.getSku(), request.getCopies(), printerName);

        } catch (Exception ex) {
            job.setStatus(PrintJob.PrintJobStatus.FAILED);
            job.setErrorMessage(ex.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            log.error("Print job {} failed for product {}: {}",
                    job.getId(), product.getSku(), ex.getMessage(), ex);
            throw new PrintJobException("Print failed: " + ex.getMessage(), ex);
        } finally {
            job = printJobRepository.save(job);
            PrintJobDTO dto = printJobMapper.toDto(job);
            boolean success = job.getStatus() == PrintJob.PrintJobStatus.COMPLETED;
            eventPublisher.publishEvent(new PrintJobCompletedEvent(this, dto, success));
        }

        return printJobMapper.toDto(job);
    }

    @Override
    @Transactional
    public Path exportPdf(BarcodeGenerationRequestDTO request, Path outputPath) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        // Ensure the barcode exists or generate it
        byte[] imagePng = barcodeGenerationService.renderImagePng(
                barcodeGenerationService.computeInternalBarcodeValue(
                        product.getId(),
                        product.getCategory() != null ? product.getCategory().getCode() : "XX"),
                request.getBarcodeType(), 400, 150);

        return pdfExporter.export(imagePng, product, request.getLabelTemplate(),
                request.getCopies(), outputPath);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrintJobDTO> getRecentHistory() {
        return printJobMapper.toDtoList(printJobRepository.findTop50ByOrderByCreatedAtDesc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrintJobDTO> getHistoryForProduct(Long productId) {
        return printJobMapper.toDtoList(
                printJobRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @Override
    public List<String> getAvailablePrinters() {
        return Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
                .map(javax.print.PrintService::getName)
                .collect(Collectors.toList());
    }
}
