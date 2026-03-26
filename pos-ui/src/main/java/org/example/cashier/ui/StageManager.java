package org.example.cashier.ui;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
@RequiredArgsConstructor
@Slf4j
public class StageManager {

    private final ApplicationContext springContext;
    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() { return primaryStage; }

    // ── Navigation ────────────────────────────────────────────────────────

    public void showLoginScreen() {
        primaryStage.setMaximized(false);
        primaryStage.setResizable(false);
        showWithFade("/fxml/login.fxml", "POS Pro — Sign In", 500, 640);
    }

    public void showCashierScreen() {
        primaryStage.setResizable(true);
        showMaximized("/fxml/main_cashier.fxml", "POS Pro — Cashier");
    }

    public void showProductManager() {
        showMaximized("/fxml/product_manager.fxml", "POS Pro — Product Manager");
    }

    public void showReports() {
        showMaximized("/fxml/reports.fxml", "POS Pro — Reports");
    }

    public void showUserManager() {
        showMaximized("/fxml/user_manager.fxml", "POS Pro — User Management");
    }

    // ── Modal dialogs ─────────────────────────────────────────────────────

    public void showCheckoutDialog() {
        showModal("/fxml/checkout.fxml", "Checkout", 520, 600);
    }

    public void showReceiptDialog() {
        showModal("/fxml/receipt.fxml", "Receipt", 540, 620);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /** Full-screen (maximized) navigation with fade transition. */
    private void showMaximized(String fxmlPath, String title) {
        try {
            Parent newRoot = load(fxmlPath);
            primaryStage.setTitle(title);

            Scene scene = primaryStage.getScene();
            if (scene == null) {
                // First paint — no fade, just show maximized
                primaryStage.setScene(new Scene(newRoot, 1280, 800));
                primaryStage.setMaximized(true);
                primaryStage.show();
                return;
            }

            // Load done — now animate
            newRoot.setOpacity(0.0);
            Parent oldRoot = scene.getRoot();

            FadeTransition out = new FadeTransition(Duration.millis(120), oldRoot);
            out.setToValue(0.0);
            out.setOnFinished(e -> {
                scene.setRoot(newRoot);
                if (!primaryStage.isMaximized()) primaryStage.setMaximized(true);
                FadeTransition in = new FadeTransition(Duration.millis(180), newRoot);
                in.setFromValue(0.0);
                in.setToValue(1.0);
                in.play();
            });
            out.play();

        } catch (IOException e) {
            log.error("Cannot load screen: {}", fxmlPath, e);
            throw new RuntimeException("Cannot load screen: " + fxmlPath, e);
        }
    }

    /** Fixed-size navigation with fade transition (used for login). */
    private void showWithFade(String fxmlPath, String title, double width, double height) {
        try {
            Parent newRoot = load(fxmlPath);
            primaryStage.setTitle(title);

            Scene scene = primaryStage.getScene();
            if (scene == null) {
                primaryStage.setScene(new Scene(newRoot, width, height));
                primaryStage.centerOnScreen();
                primaryStage.show();
                return;
            }

            newRoot.setOpacity(0.0);
            Parent oldRoot = scene.getRoot();

            FadeTransition out = new FadeTransition(Duration.millis(120), oldRoot);
            out.setToValue(0.0);
            out.setOnFinished(e -> {
                scene.setRoot(newRoot);
                primaryStage.setWidth(width);
                primaryStage.setHeight(height);
                primaryStage.centerOnScreen();
                FadeTransition in = new FadeTransition(Duration.millis(180), newRoot);
                in.setFromValue(0.0);
                in.setToValue(1.0);
                in.play();
            });
            out.play();

        } catch (IOException e) {
            log.error("Cannot load screen: {}", fxmlPath, e);
            throw new RuntimeException("Cannot load screen: " + fxmlPath, e);
        }
    }

    /** Modal dialog — no fade needed, opens over the maximized window. */
    private void showModal(String fxmlPath, String title, double width, double height) {
        try {
            Parent root = load(fxmlPath);
            Stage dialog = new Stage();
            dialog.initOwner(primaryStage);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle(title);
            dialog.setScene(new Scene(root, width, height));
            dialog.setResizable(false);
            dialog.centerOnScreen();
            dialog.showAndWait();
        } catch (IOException e) {
            log.error("Cannot load dialog: {}", fxmlPath, e);
            throw new RuntimeException("Cannot load dialog: " + fxmlPath, e);
        }
    }

    private Parent load(String fxmlPath) throws IOException {
        URL fxml = getClass().getResource(fxmlPath);
        if (fxml == null) {
            throw new IllegalStateException(fxmlPath + " not found on classpath");
        }
        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setControllerFactory(springContext::getBean);
        return loader.load();
    }
}
