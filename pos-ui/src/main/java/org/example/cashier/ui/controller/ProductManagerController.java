package org.example.cashier.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.enums.UserRole;
import org.example.cashier.services.CashierProductService;
import org.example.cashier.ui.AlertHelper;
import org.example.cashier.ui.SessionState;
import org.example.cashier.ui.StageManager;
import org.example.cashier.ui.util.CurrencyFormatter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ProductManagerController implements Initializable {

    @FXML private TextField  searchField;
    @FXML private TableView<Product>          productTable;
    @FXML private TableColumn<Product,String> colCode;
    @FXML private TableColumn<Product,String> colProdName;
    @FXML private TableColumn<Product,String> colProdPrice;
    @FXML private TableColumn<Product,String> colProdStock;
    @FXML private ImageView  barcodePreview;

    @FXML private TextField  fldCode;
    @FXML private TextField  fldName;
    @FXML private TextField  fldPrice;
    @FXML private TextField  fldStock;
    @FXML private TextArea   fldDesc;

    private final CashierProductService productService;
    private final StageManager          stageManager;
    private final SessionState          sessionState;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        enforceRole();

        colCode.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getSku()));
        colProdName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getName()));
        colProdPrice.setCellValueFactory(c ->
                new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getPrice())));
        colProdStock.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getStockQuantity())));

        productTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) populateForm(selected);
                });

        // SKU field is read-only — always auto-generated
        fldCode.setEditable(false);
        fldCode.setStyle(fldCode.getStyle() + " -fx-background-color: #f5f5f5;");

        loadProducts();
    }

    private void enforceRole() {
        var user = sessionState.getCurrentUser();
        if (user == null
                || (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.MANAGER)) {
            AlertHelper.showWarning("Access Denied",
                    "Only Admin and Manager roles can access Product Manager.");
            stageManager.showCashierScreen();
        }
    }

    private void loadProducts() {
        productTable.setItems(FXCollections.observableArrayList(productService.findAll()));
    }

    /** Currently displayed product — used by the print handler. */
    private Product currentProduct;

    private void populateForm(Product p) {
        currentProduct = p;
        fldCode.setText(p.getSku());
        fldName.setText(p.getName());
        fldPrice.setText(p.getPrice().toPlainString());
        fldStock.setText(String.valueOf(p.getStockQuantity()));
        fldDesc.setText(p.getDescription() != null ? p.getDescription() : "");
        showBarcodePreview(p);
    }

    private void showBarcodePreview(Product p) {
        try {
            byte[] labelBytes = productService.getBarcodeLabel(p);
            barcodePreview.setImage(new Image(new ByteArrayInputStream(labelBytes)));
        } catch (Exception e) {
            barcodePreview.setImage(null);
        }
    }

    @FXML
    void onSearch() {
        String q = searchField.getText().trim();
        productTable.setItems(FXCollections.observableArrayList(productService.search(q)));
    }

    @FXML
    void onAdd() {
        clearForm();
        // Auto-generate SKU for new product
        fldCode.setText(productService.generateSku());
        fldName.requestFocus();
    }

    @FXML
    void onSave() {
        try {
            String code  = fldCode.getText().trim();
            String name  = fldName.getText().trim();
            String price = fldPrice.getText().trim();
            String stock = fldStock.getText().trim();

            if (code.isEmpty() || name.isEmpty() || price.isEmpty() || stock.isEmpty()) {
                AlertHelper.showWarning("Validation", "Name, Price and Stock are required.");
                return;
            }

            Product selected = productTable.getSelectionModel().getSelectedItem();
            Product p;
            if (selected != null) {
                p = selected;
            } else {
                p = Product.builder()
                        .sku(code)
                        .build();
            }
            p.setSku(code);
            p.setName(name);
            p.setPrice(new BigDecimal(price));
            p.setStockQuantity(Integer.parseInt(stock));
            p.setDescription(fldDesc.getText().trim());

            boolean isNew = (selected == null);
            p = productService.save(p);
            currentProduct = p;
            // For existing products the barcode value (SKU) never changes — only refresh
            // the label image so name/price reflect the latest values.
            showBarcodePreview(p);
            AlertHelper.showInfo("Saved", isNew
                    ? "Product created. Barcode generated."
                    : "Product updated. Barcode unchanged.");
            loadProducts();
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Invalid Input", "Price and Stock must be valid numbers.");
        }
    }

    @FXML
    void onDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Nothing Selected", "Select a product to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selected.getName() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            productService.delete(selected.getId());
            loadProducts();
            clearForm();
        });
    }

    @FXML
    void onPrintBarcode() {
        if (currentProduct == null) {
            AlertHelper.showWarning("No Product", "Select a product to print its barcode.");
            return;
        }
        Image img = barcodePreview.getImage();
        if (img == null) {
            AlertHelper.showWarning("No Barcode", "Barcode preview is empty.");
            return;
        }

        // Create a sized ImageView for printing (keeps the label dimensions)
        ImageView printView = new ImageView(img);
        printView.setFitWidth(400);
        printView.setPreserveRatio(true);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            AlertHelper.showError("Printer Error", "No printer available.");
            return;
        }
        boolean proceed = job.showPrintDialog(barcodePreview.getScene().getWindow());
        if (proceed) {
            boolean success = job.printPage(printView);
            if (success) {
                job.endJob();
                AlertHelper.showInfo("Printed", "Barcode label sent to printer.");
            } else {
                AlertHelper.showError("Print Failed", "Could not print the barcode.");
            }
        }
    }

    @FXML
    void onBack() {
        stageManager.showCashierScreen();
    }

    private void clearForm() {
        currentProduct = null;
        productTable.getSelectionModel().clearSelection();
        fldCode.clear();
        fldName.clear();
        fldPrice.clear();
        fldStock.clear();
        fldDesc.clear();
        barcodePreview.setImage(null);
    }
}
