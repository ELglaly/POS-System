package org.example.cashier.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.cashier.core.entity.Transaction;
import org.example.cashier.core.model.CartItem;
import org.example.cashier.services.CartService;
import org.example.cashier.services.CheckoutService;
import org.example.cashier.services.ReceiptService;
import org.example.cashier.ui.AlertHelper;
import org.example.cashier.ui.SessionState;
import org.example.cashier.ui.StageManager;
import org.example.cashier.ui.util.CurrencyFormatter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CheckoutController implements Initializable {

    @FXML private TableView<CartItem>          summaryTable;
    @FXML private TableColumn<CartItem,String> colItem;
    @FXML private TableColumn<CartItem,String> colQtySum;
    @FXML private TableColumn<CartItem,String> colSumTotal;
    @FXML private Label     lblSubtotal;
    @FXML private Label     lblDiscount;
    @FXML private Label     lblTotal;
    @FXML private Label     lblChange;
    @FXML private RadioButton rbCash;
    @FXML private RadioButton rbCard;
    @FXML private TextField   amountField;
    @FXML private HBox        cashBox;

    private final CartService     cartService;
    private final CheckoutService checkoutService;
    private final ReceiptService  receiptService;
    private final StageManager    stageManager;
    private final SessionState    sessionState;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        updateTotals();

        rbCash.selectedProperty().addListener((obs, old, val) -> {
            cashBox.setVisible(val);
            cashBox.setManaged(val);
            lblChange.setText("$0.00");
        });
    }

    private void setupTable() {
        colItem.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getProduct().getName()));
        colQtySum.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colSumTotal.setCellValueFactory(c ->
                new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getSubtotal())));
        summaryTable.setItems(cartService.getItems());
    }

    private void updateTotals() {
        lblSubtotal.setText(CurrencyFormatter.format(cartService.getSubtotal()));
        lblDiscount.setText(CurrencyFormatter.format(cartService.getDiscountAmount()));
        lblTotal.setText(CurrencyFormatter.format(cartService.getTotal()));
    }

    @FXML
    void onAmountChanged() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            BigDecimal change = amount.subtract(cartService.getTotal());
            lblChange.setText(change.compareTo(BigDecimal.ZERO) >= 0
                    ? CurrencyFormatter.format(change) : "Insufficient");
        } catch (NumberFormatException e) {
            lblChange.setText("—");
        }
    }

    @FXML
    void onConfirm() {
        Transaction.PaymentMethod method = rbCard.isSelected()
                ? Transaction.PaymentMethod.CARD
                : Transaction.PaymentMethod.CASH;

        BigDecimal tendered = null;
        if (method == Transaction.PaymentMethod.CASH) {
            try {
                tendered = new BigDecimal(amountField.getText().trim());
            } catch (NumberFormatException e) {
                AlertHelper.showWarning("Invalid Amount", "Enter the cash amount received.");
                return;
            }
        }

        try {
            Transaction tx = checkoutService.checkout(
                    sessionState.getCurrentUser(), method, tendered);
            receiptService.printReceipt(tx);

            sessionState.setLastTransaction(tx);

            AlertHelper.showInfo("Success",
                    "Transaction #" + tx.getId() + " completed.\nReceipt sent to printer.");
            close();
            stageManager.showReceiptDialog();

        } catch (Exception e) {
            AlertHelper.showError("Checkout Failed", e.getMessage());
        }
    }

    @FXML
    void onCancel() { close(); }

    private void close() {
        ((Stage) summaryTable.getScene().getWindow()).close();
    }
}
