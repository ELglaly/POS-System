package org.example.cashier.ui.control;

import javafx.animation.ScaleTransition;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.example.cashier.core.dto.BarcodeDTO;

import java.util.function.Consumer;

/**
 * Reusable custom JavaFX control that renders a single barcode.
 *
 * Layout:
 * ┌─────────────────────────────────┐
 * │  [barcode image 300x120px]      │
 * │  Product Name (truncated)       │
 * │  $Price  |  barcode value       │
 * │  [Print] [Download PNG] [Copy]  │
 * └─────────────────────────────────┘
 *
 * Responds to hover: scale 1.0 → 1.02, deeper drop shadow.
 * CSS class: .barcode-widget
 */
public class BarcodeWidget extends VBox {

    private final ObjectProperty<BarcodeDTO> barcodeProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Image>      imageProperty   = new SimpleObjectProperty<>();

    private final ImageView imageView;
    private final Label     productNameLabel;
    private final Label     barcodeValueLabel;
    private final Label     priceLabel;
    private final Button    printButton;
    private final Button    downloadButton;
    private final Button    copyButton;

    /** Callback — invoked when Print is clicked. */
    private Consumer<BarcodeDTO> onPrint;
    /** Callback — invoked when Download PNG is clicked. */
    private Consumer<BarcodeDTO> onDownload;

    public BarcodeWidget() {
        getStyleClass().add("barcode-widget");
        setSpacing(8);
        setPadding(new Insets(16));
        setAlignment(Pos.CENTER);
        setMinWidth(320);
        setMaxWidth(360);

        // ── Image ────────────────────────────────────────────────────────────
        imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.getStyleClass().add("barcode-image");
        imageProperty.addListener((obs, old, img) -> imageView.setImage(img));

        // ── Text labels ──────────────────────────────────────────────────────
        productNameLabel = new Label("—");
        productNameLabel.getStyleClass().add("barcode-product-name");
        productNameLabel.setTextAlignment(TextAlignment.CENTER);
        productNameLabel.setMaxWidth(Double.MAX_VALUE);
        productNameLabel.setWrapText(false);
        productNameLabel.setEllipsisString("…");

        priceLabel = new Label();
        priceLabel.getStyleClass().add("barcode-price");

        barcodeValueLabel = new Label();
        barcodeValueLabel.getStyleClass().add("barcode-value-label");

        HBox metaRow = new HBox(12, priceLabel, new Separator(javafx.geometry.Orientation.VERTICAL), barcodeValueLabel);
        metaRow.setAlignment(Pos.CENTER);

        // ── Action strip ─────────────────────────────────────────────────────
        printButton    = createActionButton("Print",        "barcode-action-print");
        downloadButton = createActionButton("Download PNG", "barcode-action-download");
        copyButton     = createActionButton("Copy Number",  "barcode-action-copy");

        copyButton.setOnAction(e -> copyBarcodeValueToClipboard());

        HBox actions = new HBox(8, printButton, downloadButton, copyButton);
        actions.setAlignment(Pos.CENTER);

        // ── Hover animation ──────────────────────────────────────────────────
        ScaleTransition hoverIn  = new ScaleTransition(Duration.millis(150), this);
        ScaleTransition hoverOut = new ScaleTransition(Duration.millis(150), this);
        hoverIn.setToX(1.02);
        hoverIn.setToY(1.02);
        hoverOut.setToX(1.0);
        hoverOut.setToY(1.0);

        setOnMouseEntered(e -> { hoverIn.playFromStart(); styleOnHover(true); });
        setOnMouseExited (e -> { hoverOut.playFromStart(); styleOnHover(false); });

        getChildren().addAll(imageView, productNameLabel, metaRow, new Separator(), actions);

        // ── Barcode property binding ─────────────────────────────────────────
        barcodeProperty.addListener((obs, old, dto) -> updateDisplay(dto));
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public ObjectProperty<BarcodeDTO> barcodeProperty()    { return barcodeProperty; }
    public ObjectProperty<Image>      imageProperty()       { return imageProperty; }

    public void setBarcode(BarcodeDTO dto)   { barcodeProperty.set(dto); }
    public BarcodeDTO getBarcode()           { return barcodeProperty.get(); }

    public void setBarcodeImage(Image image) { imageProperty.set(image); }

    public void setOnPrint(Consumer<BarcodeDTO> handler) {
        this.onPrint = handler;
        printButton.setOnAction(e -> { if (onPrint != null && getBarcode() != null) onPrint.accept(getBarcode()); });
    }

    public void setOnDownload(Consumer<BarcodeDTO> handler) {
        this.onDownload = handler;
        downloadButton.setOnAction(e -> { if (onDownload != null && getBarcode() != null) onDownload.accept(getBarcode()); });
    }

    public void clearDisplay() {
        imageView.setImage(null);
        productNameLabel.setText("—");
        priceLabel.setText("");
        barcodeValueLabel.setText("");
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void updateDisplay(BarcodeDTO dto) {
        if (dto == null) { clearDisplay(); return; }
        productNameLabel.setText(dto.getProductName() != null ? dto.getProductName() : "—");
        barcodeValueLabel.setText(dto.getBarcodeValue());
    }

    private void copyBarcodeValueToClipboard() {
        BarcodeDTO dto = getBarcode();
        if (dto == null || dto.getBarcodeValue() == null) return;
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(dto.getBarcodeValue());
        clipboard.setContent(content);
    }

    private void styleOnHover(boolean hovered) {
        if (hovered) {
            setStyle("-fx-border-color: rgba(0,200,150,0.4); -fx-border-width: 1; -fx-border-radius: 12; "
                   + "-fx-background-color: #1E2330; -fx-background-radius: 12; "
                   + "-fx-effect: dropshadow(gaussian, rgba(0,200,150,0.15), 20, 0, 0, 0);");
        } else {
            setStyle("-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1; -fx-border-radius: 12; "
                   + "-fx-background-color: #161A23; -fx-background-radius: 12;");
        }
    }

    private Button createActionButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("pos-button-ghost", styleClass);
        btn.setMinWidth(90);
        // Tactile press animation
        ScaleTransition press = new ScaleTransition(Duration.millis(80), btn);
        press.setFromX(1.0); press.setFromY(1.0);
        press.setToX(0.96);  press.setToY(0.96);
        press.setAutoReverse(true);
        press.setCycleCount(2);
        btn.setOnMousePressed(e -> press.playFromStart());
        return btn;
    }
}
