package org.example.cashier.core.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.cashier.core.constants.BarcodeConstants;
import org.example.cashier.core.enums.BarcodeType;
import org.example.cashier.core.enums.LabelTemplate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarcodeGenerationRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Barcode type is required")
    private BarcodeType barcodeType;

    @NotNull(message = "Label template is required")
    @Builder.Default
    private LabelTemplate labelTemplate = LabelTemplate.STANDARD;

    @Min(value = 1, message = "Must print at least 1 copy")
    @Max(value = BarcodeConstants.MAX_COPIES_PER_JOB,
         message = "Cannot exceed " + BarcodeConstants.MAX_COPIES_PER_JOB + " copies per job")
    @Builder.Default
    private int copies = 1;
    @Size(max = 40)
    private String customOverlayText;
    @NotBlank(message = "Operator username is required")
    private String operatorUsername;

    @Builder.Default
    private boolean deactivatePrevious = true;
}
