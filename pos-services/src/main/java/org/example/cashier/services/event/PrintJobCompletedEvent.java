package org.example.cashier.services.event;

import lombok.Getter;
import org.example.cashier.core.dto.PrintJobDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Published when a print job reaches a terminal status (COMPLETED or FAILED).
 * Drives the UI toast notification and history table refresh.
 */
@Getter
public class PrintJobCompletedEvent extends ApplicationEvent {

    private final PrintJobDTO printJob;
    private final boolean success;

    public PrintJobCompletedEvent(Object source, PrintJobDTO printJob, boolean success) {
        super(source);
        this.printJob = printJob;
        this.success = success;
    }
}
