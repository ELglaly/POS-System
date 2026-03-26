package org.example.cashier.ui.scanner;

import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * QR/barcode scanner service.
 * Camera scanning requires the webcam-capture library (not currently bundled).
 * USB barcode scanners work via the manual TextField input — no camera needed.
 */
@Component
@Slf4j
public class QRScannerService {

    private Consumer<String> onCodeDetected;
    private Consumer<Image>  onFrameUpdate;
    private Consumer<String> onError;

    public void setOnCodeDetected(Consumer<String> callback) { this.onCodeDetected = callback; }
    public void setOnFrameUpdate(Consumer<Image>   callback) { this.onFrameUpdate  = callback; }
    public void setOnError(Consumer<String>        callback) { this.onError        = callback; }

    public void startScanning(int cameraIndex) {
        log.warn("Camera scanning not available. Use a USB barcode scanner or type the code manually.");
        if (onError != null) {
            onError.accept("Camera scanning is not available in this build. "
                    + "Use a USB barcode scanner or type the product SKU and press Enter.");
        }
    }

    public void stopScanning() {
        // no-op
    }

    public boolean isRunning() { return false; }

    public void shutdown() {
        // no-op
    }
}
