package org.example.cashier.core.dto;

import lombok.*;
import org.example.cashier.core.entity.PrintJob;
import org.example.cashier.core.enums.BarcodeType;
import org.example.cashier.core.enums.LabelTemplate;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintJobDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private BarcodeType barcodeType;
    private LabelTemplate labelTemplate;
    private int copies;
    private String printerName;
    private String operatorUsername;
    private PrintJob.PrintJobStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
