package org.example.cashier.ui.control;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Slide-in toast notification shown bottom-right.
 * Supports SUCCESS, ERROR, WARNING, INFO variants with coloured left borders.
 * Auto-dismisses after 3 seconds with a fade-out.  Queues overflow beyond 3.
 *
 * Usage (always call from JavaFX thread):
 * <pre>
 *   NotificationToast.show(ownerWindow, Type.SUCCESS, "Saved", "Barcode saved successfully");
 * </pre>
 */
public class NotificationToast extends StackPane {

    public enum Type {
        SUCCESS("#00C896"),
        ERROR  ("#FF4D4F"),
        WARNING("#FFB020"),
        INFO   ("#3D8EFF");

        private final String hex;
        Type(String hex) { this.hex = hex; }
        public String getHex() { return hex; }
    }

    private static final int    MAX_VISIBLE     = 3;
    private static final double TOAST_WIDTH     = 320.0;
    private static final double TOAST_HEIGHT    = 72.0;
    private static final double MARGIN_RIGHT    = 20.0;
    private static final double MARGIN_BOTTOM   = 20.0;
    private static final int    STACK_OFFSET_Y  = 80;  // vertical gap between stacked toasts

    /** All currently visible toasts (max MAX_VISIBLE). */
    private static final Deque<Popup> visibleToasts = new ArrayDeque<>();
    /** Queued toasts waiting for a slot. */
    private static final Deque<Runnable> pendingQueue = new ArrayDeque<>();

    // ── Static factory ────────────────────────────────────────────────────────

    public static void show(Window owner, Type type, String title, String subtitle) {
        if (visibleToasts.size() >= MAX_VISIBLE) {
            pendingQueue.add(() -> show(owner, type, title, subtitle));
            return;
        }
        createAndShow(owner, type, title, subtitle);
    }

    // ── Construction ──────────────────────────────────────────────────────────

    private static void createAndShow(Window owner, Type type, String title, String subtitle) {
        NotificationToast toast = new NotificationToast(type, title, subtitle);
        Popup popup = new Popup();
        popup.getContent().add(toast);
        popup.setAutoFix(false);

        // Position: bottom-right relative to owner window, stacked above existing toasts
        double x = owner.getX() + owner.getWidth()  - TOAST_WIDTH  - MARGIN_RIGHT;
        double y = owner.getY() + owner.getHeight()  - TOAST_HEIGHT - MARGIN_BOTTOM
                   - visibleToasts.size() * STACK_OFFSET_Y;

        popup.show(owner, x, y);
        visibleToasts.push(popup);

        // Slide in from right
        toast.setTranslateX(TOAST_WIDTH + 20);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), toast);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        // After 3 seconds, fade out and remove
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(3));
        fadeOut.setOnFinished(e -> {
            popup.hide();
            visibleToasts.remove(popup);
            // Show next queued toast if any
            if (!pendingQueue.isEmpty()) {
                pendingQueue.poll().run();
            }
        });

        new SequentialTransition(slideIn, fadeOut).play();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private NotificationToast(Type type, String title, String subtitle) {
        // Left accent border
        Rectangle accent = new Rectangle(4, TOAST_HEIGHT);
        accent.setFill(Color.web(type.getHex()));

        // Text content
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("toast-title");
        titleLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #F0F2F8;");

        VBox textBox = new VBox(2, titleLabel);
        textBox.setPadding(new Insets(0, 12, 0, 12));
        textBox.setAlignment(Pos.CENTER_LEFT);

        if (subtitle != null && !subtitle.isBlank()) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8892A4;");
            subtitleLabel.setWrapText(true);
            subtitleLabel.setMaxWidth(TOAST_WIDTH - 80);
            textBox.getChildren().add(subtitleLabel);
        }

        HBox content = new HBox(accent, textBox);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPrefSize(TOAST_WIDTH, TOAST_HEIGHT);
        content.setStyle("""
                -fx-background-color: #1E2330;
                -fx-background-radius: 8;
                -fx-border-color: rgba(255,255,255,0.08);
                -fx-border-width: 1;
                -fx-border-radius: 8;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 16, 0, 0, 4);
                """);

        getChildren().add(content);
        setAlignment(Pos.CENTER);

        // Button press scale animation on the accent bar (tactile feedback)
        ScaleTransition pop = new ScaleTransition(Duration.millis(100), accent);
        pop.setFromY(0.9);
        pop.setToY(1.0);
        pop.play();
    }
}
