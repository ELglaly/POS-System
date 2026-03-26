package org.example.cashier.core.dto;

import lombok.*;
import org.example.cashier.core.enums.BarcodeType;

import java.time.LocalDateTime;

/**
 * Barcode record DTO — returned from service layer to UI and API consumers.
 * Never exposes the entity directly; the UI controller binds to this.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarcodeDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String barcodeValue;
    private BarcodeType barcodeType;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private boolean active;

    /**
     * Transient field — populated by the service after generation.
     * Not persisted; used to pass the rendered image to the UI in one call.
     */
    private transient byte[] renderedImagePng;
}
