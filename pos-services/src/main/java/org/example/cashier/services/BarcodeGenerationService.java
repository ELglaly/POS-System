package org.example.cashier.services;

import org.example.cashier.core.dto.BarcodeDTO;
import org.example.cashier.core.dto.BarcodeGenerationRequestDTO;
import org.example.cashier.core.enums.BarcodeType;

import java.util.List;
import java.util.Optional;

/**
 * Contract for barcode generation, value computation, and retrieval.
 * Implementations must publish {@code BarcodeGeneratedEvent} after each successful generation.
 */
public interface BarcodeGenerationService {

    /**
     * Generate a new barcode for the product specified in the request.
     * The rendered PNG image is included in the returned DTO's {@code renderedImagePng} field.
     *
     * @throws org.example.cashier.core.exception.ProductNotFoundException if the product ID is invalid
     * @throws org.example.cashier.core.exception.BarcodeGenerationException on ZXing encoding failure
     */
    BarcodeDTO generate(BarcodeGenerationRequestDTO request);

    /** Retrieve all active barcodes for a product. */
    List<BarcodeDTO> findActiveByProduct(Long productId);

    /** Look up a persisted barcode record by its value string. */
    Optional<BarcodeDTO> findByValue(String barcodeValue);

    /** Regenerate the image bytes for an existing barcode record without creating a new DB row. */
    byte[] renderImagePng(String barcodeValue, BarcodeType type, int widthPx, int heightPx);

    /**
     * Compute the POS-internal barcode value for a product.
     * Format: {@code POS-{categoryCode}-{productId}-{checkDigit}}
     * Pure computation — no side effects, no DB access.
     */
    String computeInternalBarcodeValue(Long productId, String categoryCode);
}
