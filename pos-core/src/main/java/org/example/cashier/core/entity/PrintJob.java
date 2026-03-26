package org.example.cashier.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.cashier.core.enums.BarcodeType;
import org.example.cashier.core.enums.LabelTemplate;

import java.time.LocalDateTime;

/**
 * Audit record for every label/barcode print operation.
 * Tracks what was printed, how many copies, which template, and who initiated.
 * Used in the barcode screen print history table.
 */
@Entity
@Table(name = "print_jobs", indexes = {
        @Index(name = "idx_printjob_created",  columnList = "created_at"),
        @Index(name = "idx_printjob_product",  columnList = "product_id"),
        @Index(name = "idx_printjob_operator", columnList = "operator_username")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "barcode_type", nullable = false, length = 10)
    private BarcodeType barcodeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_template", nullable = false, length = 20)
    @Builder.Default
    private LabelTemplate labelTemplate = LabelTemplate.STANDARD;

    @Column(nullable = false)
    @Builder.Default
    private int copies = 1;

    /** Name of the printer device used. */
    @Column(name = "printer_name", length = 120)
    private String printerName;

    @Column(name = "operator_username", nullable = false, length = 60)
    private String operatorUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private PrintJobStatus status = PrintJobStatus.PENDING;

    /** Populated when status = FAILED. */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum PrintJobStatus { PENDING, PRINTING, COMPLETED, FAILED }
}