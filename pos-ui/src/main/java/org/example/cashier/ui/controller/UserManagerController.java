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
import org.example.cashier.core.entity.User;
import org.example.cashier.core.enums.UserRole;
import org.example.cashier.services.UserManagementService;
import org.example.cashier.ui.AlertHelper;
import org.example.cashier.ui.SessionState;
import org.example.cashier.ui.StageManager;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class UserManagerController implements Initializable {

    @FXML private TableView<User>          userTable;
    @FXML private TableColumn<User,String> colUsername;
    @FXML private TableColumn<User,String> colFullName;
    @FXML private TableColumn<User,String> colRole;
    @FXML private TableColumn<User,String> colStatus;

    @FXML private TextField     fldUsername;
    @FXML private TextField     fldFullName;
    @FXML private ChoiceBox<UserRole> roleChoice;
    @FXML private PasswordField fldPassword;
    @FXML private PasswordField fldConfirmPassword;
    @FXML private ImageView     barcodePreview;

    private final UserManagementService userService;
    private final StageManager          stageManager;
    private final SessionState          sessionState;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    /** Currently selected / being-edited user. Null when creating a new one. */
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        enforceAdmin();

        colUsername.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getUsername()));
        colFullName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFullName() != null
                        ? c.getValue().getFullName() : ""));
        colRole.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRole().name()));
        colStatus.setCellValueFactory(c -> {
            var last = c.getValue().getLastLoginAt();
            return new SimpleStringProperty(last != null ? last.format(DT_FMT) : "Never");
        });

        roleChoice.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleChoice.setValue(UserRole.CASHIER);

        userTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> { if (selected != null) populateForm(selected); });

        loadUsers();
    }

    private void enforceAdmin() {
        var user = sessionState.getCurrentUser();
        if (user == null || user.getRole() != UserRole.ADMIN) {
            AlertHelper.showWarning("Access Denied", "Only Admin can access User Management.");
            stageManager.showCashierScreen();
        }
    }

    private void loadUsers() {
        userTable.setItems(FXCollections.observableArrayList(userService.findAll()));
    }

    private void populateForm(User u) {
        currentUser = u;
        fldUsername.setText(u.getUsername());
        fldFullName.setText(u.getFullName() != null ? u.getFullName() : "");
        roleChoice.setValue(u.getRole());
        fldPassword.clear();
        fldConfirmPassword.clear();
        showBarcodePreview(u);
    }

    private void showBarcodePreview(User u) {
        try {
            byte[] bytes = userService.getUserBarcodeLabel(u);
            barcodePreview.setImage(new Image(new ByteArrayInputStream(bytes)));
        } catch (Exception e) {
            barcodePreview.setImage(null);
        }
    }

    @FXML
    void onNew() {
        currentUser = null;
        userTable.getSelectionModel().clearSelection();
        fldUsername.clear();
        fldFullName.clear();
        roleChoice.setValue(UserRole.CASHIER);
        fldPassword.clear();
        fldConfirmPassword.clear();
        barcodePreview.setImage(null);
        fldUsername.requestFocus();
    }

    @FXML
    void onSave() {
        String username = fldUsername.getText().trim();
        String fullName = fldFullName.getText().trim();
        UserRole role   = roleChoice.getValue();
        String password = fldPassword.getText();
        String confirm  = fldConfirmPassword.getText();

        if (username.isEmpty()) {
            AlertHelper.showWarning("Validation", "Username is required.");
            return;
        }
        if (!password.equals(confirm)) {
            AlertHelper.showWarning("Validation", "Passwords do not match.");
            return;
        }

        try {
            User saved;
            if (currentUser == null) {
                // Creating a new user — password is mandatory
                if (password.isBlank()) {
                    AlertHelper.showWarning("Validation", "Password is required for a new user.");
                    return;
                }
                saved = userService.createUser(username, fullName.isEmpty() ? null : fullName, password, role);
                AlertHelper.showInfo("Created", "User \"" + saved.getUsername() + "\" created. Staff badge generated.");
            } else {
                saved = userService.updateUser(
                        currentUser.getId(), username,
                        fullName.isEmpty() ? null : fullName,
                        password.isBlank() ? null : password,
                        role);
                AlertHelper.showInfo("Saved", "User \"" + saved.getUsername() + "\" updated.");
            }
            currentUser = saved;
            showBarcodePreview(saved);
            loadUsers();
        } catch (IllegalArgumentException ex) {
            AlertHelper.showWarning("Error", ex.getMessage());
        }
    }

    @FXML
    void onDelete() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Nothing Selected", "Select a user to delete.");
            return;
        }
        // Prevent deleting yourself
        var me = sessionState.getCurrentUser();
        if (me != null && me.getId().equals(selected.getId())) {
            AlertHelper.showWarning("Not Allowed", "You cannot delete your own account.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Deactivate user \"" + selected.getUsername() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            userService.deleteUser(selected.getId());
            onNew();
            loadUsers();
        });
    }

    @FXML
    void onPrintBarcode() {
        if (currentUser == null) {
            AlertHelper.showWarning("No User", "Select or save a user first.");
            return;
        }
        Image img = barcodePreview.getImage();
        if (img == null) {
            AlertHelper.showWarning("No Badge", "Badge preview is empty.");
            return;
        }

        ImageView printView = new ImageView(img);
        printView.setFitWidth(400);
        printView.setPreserveRatio(true);

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            AlertHelper.showError("Printer Error", "No printer available.");
            return;
        }
        if (job.showPrintDialog(barcodePreview.getScene().getWindow())) {
            if (job.printPage(printView)) {
                job.endJob();
                AlertHelper.showInfo("Printed", "Staff badge sent to printer.");
            } else {
                AlertHelper.showError("Print Failed", "Could not print the badge.");
            }
        }
    }

    @FXML
    void onBack() {
        stageManager.showCashierScreen();
    }
}
