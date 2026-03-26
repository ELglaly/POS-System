package org.example.cashier.services.event;

import lombok.Getter;
import org.example.cashier.core.dto.BarcodeDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Published via {@code ApplicationEventPublisher} when a barcode is successfully
 * generated and persisted.  Subscribers (e.g. the JavaFX UI controller) listen on
 * the JavaFX thread via {@code Platform.runLater()} inside their handler.
 */
@Getter
public class BarcodeGeneratedEvent extends ApplicationEvent {

    private final BarcodeDTO barcode;

    public BarcodeGeneratedEvent(Object source, BarcodeDTO barcode) {
        super(source);
        this.barcode = barcode;
    }
}
