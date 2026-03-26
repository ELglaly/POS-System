package org.example.cashier.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.example.cashier.core.discount.NoDiscount;
import org.example.cashier.core.discount.PercentageDiscount;
import org.example.cashier.core.entity.Product;
import org.example.cashier.core.enums.UserRole;
import org.example.cashier.core.model.CartItem;
import org.example.cashier.services.CartService;
import org.example.cashier.services.ProductService;
import org.example.cashier.ui.AlertHelper;
import org.example.cashier.ui.SessionState;
import org.example.cashier.ui.StageManager;
import org.example.cashier.ui.util.CurrencyFormatter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class MainCashierController implements Initializable {

    // ── FXML bindings ──────────────────────────────────────────────────────
    @FXML private TextField  barcodeField;
    @FXML private TextField  searchField;
    @FXML private Label      lastScannedLabel;

    @FXML private Label      lblProductName;
    @FXML private Label      lblProductCode;
    @FXML private Label      lblProductPrice;
    @FXML private Label      lblProductStock;
    @FXML private Spinner<Integer> qtySpinner;

    @FXML private TableView<CartItem>          cartTable;
    @FXML private TableColumn<CartItem,String> colName;
    @FXML private TableColumn<CartItem,String> colQty;
    @FXML private TableColumn<CartItem,String> colPrice;
    @FXML private TableColumn<CartItem,String> colSubtotal;

    @FXML private TextField discountField;
    @FXML private Label     lblSubtotal;
    @FXML private Label     lblDiscount;
    @FXML private Label     lblTotal;

    @FXML private Button btnProductManager;
    @FXML private Button btnReports;
    @FXML private Button btnUserManager;
    @FXML private Label  lblOperator;

    // ── Spring-injected services ───────────────────────────────────────────
    private final ProductService productService;
    private final CartService           cartService;
    private final StageManager          stageManager;
    private final SessionState          sessionState;

    private Product selectedProduct;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCartTable();
        refreshTotals();
        applyRoleRestrictions();
    }

    private void setupCartTable() {
        colName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getProduct().getName()));
        colQty.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colPrice.setCellValueFactory(c ->
                new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getProduct().getPrice())));
        colSubtotal.setCellValueFactory(c ->
                new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getSubtotal())));
        cartTable.setItems(cartService.getItems());
    }

    private void applyRoleRestrictions() {
        var user = sessionState.getCurrentUser();
        if (user != null) {
            lblOperator.setText("Operator: " +
                    (user.getFullName() != null ? user.getFullName() : user.getUsername()));
            boolean isAdminOrManager = user.getRole() == UserRole.ADMIN
                    || user.getRole() == UserRole.MANAGER;
            boolean isAdmin = user.getRole() == UserRole.ADMIN;
            btnProductManager.setVisible(isAdminOrManager);
            btnProductManager.setManaged(isAdminOrManager);
            btnReports.setVisible(isAdminOrManager);
            btnReports.setManaged(isAdminOrManager);
            btnUserManager.setVisible(isAdmin);
            btnUserManager.setManaged(isAdmin);
        }
    }

    private void displayProduct(Product p) {
        selectedProduct = p;
        lblProductName.setText(p.getName());
        lblProductCode.setText(p.getSku());
        lblProductPrice.setText(CurrencyFormatter.format(p.getPrice()));
        lblProductStock.setText(String.valueOf(p.getStockQuantity()));
    }

    private boolean lookupAndDisplayProduct(String code) {
        Optional<Product> opt = productService.findBySku(code);
        if (opt.isPresent()) {
            displayProduct(opt.get());
            return true;
        } else {
            selectedProduct = null;
            clearProductDetails();
            AlertHelper.showWarning("Product Not Found",
                    "No active product found with SKU: " + code);
            return false;
        }
    }

    private void autoAddToCart() {
        if (selectedProduct == null) return;
        try {
            cartService.addItem(selectedProduct, 1);
            refreshTotals();
        } catch (IllegalStateException e) {
            AlertHelper.showWarning("Stock Warning", e.getMessage());
        }
    }

    private void clearProductDetails() {
        lblProductName.setText("—");
        lblProductCode.setText("—");
        lblProductPrice.setText("—");
        lblProductStock.setText("—");
    }

    private void refreshTotals() {
        lblSubtotal.setText(CurrencyFormatter.format(cartService.getSubtotal()));
        lblDiscount.setText(CurrencyFormatter.format(cartService.getDiscountAmount()));
        lblTotal.setText(CurrencyFormatter.format(cartService.getTotal()));
    }

    // ── FXML event handlers ────────────────────────────────────────────────

    @FXML
    void onBarcodeEntered() {
        String code = barcodeField.getText().trim();
        if (!code.isEmpty()) {
            lastScannedLabel.setText(code);
            if (lookupAndDisplayProduct(code)) {
                autoAddToCart();
            }
            barcodeField.clear();
        }
    }

    @FXML
    void onSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        List<Product> results = productService.search(query);
        if (results.isEmpty()) {
            AlertHelper.showWarning("Not Found", "No products match: " + query);
        } else if (results.size() == 1) {
            displayProduct(results.get(0));
        } else {
            ChoiceDialog<Product> dialog = new ChoiceDialog<>(results.get(0), results);
            dialog.setTitle("Select Product");
            dialog.setHeaderText("Multiple products found:");
            dialog.setContentText("Choose:");
            dialog.showAndWait().ifPresent(this::displayProduct);
        }
        searchField.clear();
    }

    @FXML
    void onManualAdd() {
        if (selectedProduct == null) {
            AlertHelper.showWarning("No Product Selected",
                    "Scan a barcode or search for a product first.");
            return;
        }
        int qty = qtySpinner.getValue();
        try {
            cartService.addItem(selectedProduct, qty);
            refreshTotals();
        } catch (IllegalStateException e) {
            AlertHelper.showWarning("Stock Warning", e.getMessage());
        }
    }

    @FXML
    void onRemoveItem() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Nothing Selected", "Select a cart item to remove.");
            return;
        }
        cartService.removeItem(selected.getProduct().getId());
        refreshTotals();
    }

    @FXML
    void onClearCart() {
        if (cartService.isEmpty()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Clear all items from the cart?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait()
               .filter(b -> b == ButtonType.YES)
               .ifPresent(b -> { cartService.clear(); refreshTotals(); });
    }

    @FXML
    void onDiscountChanged() {
        String text = discountField.getText().trim();
        try {
            double pct = text.isEmpty() ? 0 : Double.parseDouble(text);
            if (pct < 0 || pct > 100) throw new NumberFormatException();
            cartService.setDiscountStrategy(
                    pct == 0 ? new NoDiscount() : new PercentageDiscount(pct));
            refreshTotals();
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Invalid Discount", "Enter a number between 0 and 100.");
            discountField.clear();
        }
    }

    @FXML
    void onCheckout() {
        if (cartService.isEmpty()) {
            AlertHelper.showWarning("Empty Cart", "Add items before checking out.");
            return;
        }
        stageManager.showCheckoutDialog();
        refreshTotals();
    }

    @FXML
    void onProductManager() {
        stageManager.showProductManager();
    }

    @FXML
    void onReports() {
        stageManager.showReports();
    }

    @FXML
    void onUserManager() {
        stageManager.showUserManager();
    }

    @FXML
    void onLogout() {
        sessionState.setCurrentUser(null);
        cartService.clear();
        stageManager.showLoginScreen();
    }
}
