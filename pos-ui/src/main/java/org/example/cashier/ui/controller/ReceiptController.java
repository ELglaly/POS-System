package org.example.cashier.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.example.cashier.core.entity.Transaction;
import org.example.cashier.services.ReceiptFormatter;
import org.example.cashier.services.ReceiptService;
import org.example.cashier.ui.AlertHelper;
import org.example.cashier.ui.SessionState;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ReceiptController implements Initializable {

    @FXML private TextArea receiptText;

    private final SessionState     sessionState;
    private final ReceiptFormatter formatter;
    private final ReceiptService   receiptService;

    private Transaction tx;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tx = sessionState.getLastTransaction();
        if (tx != null) {
            List<String> lines = formatter.format(tx);
            receiptText.setText(String.join("\n", lines));
        } else {
            receiptText.setText("No recent transaction found.");
        }
    }

    @FXML
    void onPrint() {
        if (tx == null) { AlertHelper.showWarning("No Receipt", "No transaction to print."); return; }
        try {
            receiptService.printReceipt(tx);
            AlertHelper.showInfo("Printed", "Receipt sent to printer.");
        } catch (Exception e) {
            AlertHelper.showError("Print Error", e.getMessage());
        }
    }

    @FXML
    void onSavePdf() {
        AlertHelper.showWarning("Not Available", "PDF export is not available in this build.");
    }

    @FXML
    void onClose() {
        ((Stage) receiptText.getScene().getWindow()).close();
    }
}
