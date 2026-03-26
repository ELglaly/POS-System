package org.example.cashier.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.example.cashier.core.entity.User;
import org.example.cashier.services.AuthService;
import org.example.cashier.ui.SessionState;
import org.example.cashier.ui.StageManager;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class LoginController implements Initializable {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     barcodeField;
    @FXML private Label         errorLabel;

    private final AuthService  authService;
    private final SessionState sessionState;
    private final StageManager stageManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usernameField.requestFocus();
    }

    @FXML
    void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        Optional<User> result = authService.login(username, password);
        if (result.isPresent()) {
            proceed(result.get());
        } else {
            showError("Invalid credentials or account locked. Try again.");
            passwordField.clear();
        }
    }

    @FXML
    void onBarcodeLogin() {
        String pin = barcodeField.getText().trim();
        if (pin.isEmpty()) {
            showError("Scan your barcode or enter your barcode PIN.");
            return;
        }

        Optional<User> result = authService.loginWithBarcode(pin);
        if (result.isPresent()) {
            proceed(result.get());
        } else {
            showError("Barcode not recognised. Contact your administrator.");
            barcodeField.clear();
        }
    }

    private void proceed(User user) {
        sessionState.setCurrentUser(user);
        stageManager.showCashierScreen();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
