package org.example.cashier.core.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.cashier.core.constants.BarcodeConstants;
import org.example.cashier.core.enums.BarcodeType;
import org.example.cashier.core.enums.LabelTemplate;

/**
 * Inbound request DTO for the barcode generation service.
 * Created by {@code BarcodeController} from the UI form values.
 */
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

    /**
     * Optional custom text to overlay on the label (e.g. expiry date, lot number).
     * Null or blank = no overlay.
     */
    @Size(max = 40)
    private String customOverlayText;

    /** Username of the operator submitting the request. */
    @NotBlank(message = "Operator username is required")
    private String operatorUsername;

    /**
     * When true, any existing active barcode of the same type for this product
     * is deactivated before generating the new one.
     */
    @Builder.Default
    private boolean deactivatePrevious = true;
}
