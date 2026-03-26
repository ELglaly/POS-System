package org.example.cashier.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.example.cashier.core.entity.Transaction;
import org.example.cashier.core.entity.TransactionItem;
import org.example.cashier.services.CashierReportService;
import org.example.cashier.services.CashierReportService.RangeSummary;
import org.example.cashier.ui.AlertHelper;
import org.example.cashier.ui.StageManager;
import org.example.cashier.ui.util.CurrencyFormatter;
import org.example.cashier.ui.util.DateUtil;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ReportsController implements Initializable {

    // ── Date range ────────────────────────────────────────────────────────
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    // ── KPI labels ────────────────────────────────────────────────────────
    @FXML private Label lblRevenue;
    @FXML private Label lblTxCount;
    @FXML private Label lblAvgOrder;
    @FXML private Label lblItemsSold;
    @FXML private Label lblDiscounts;

    // ── Transaction table ─────────────────────────────────────────────────
    @FXML private TableView<Transaction>          txTable;
    @FXML private TableColumn<Transaction,String> colTxId;
    @FXML private TableColumn<Transaction,String> colTxDate;
    @FXML private TableColumn<Transaction,String> colTxCashier;
    @FXML private TableColumn<Transaction,String> colTxSubtotal;
    @FXML private TableColumn<Transaction,String> colTxDiscount;
    @FXML private TableColumn<Transaction,String> colTxTotal;
    @FXML private TableColumn<Transaction,String> colTxMethod;

    // ── Items detail table ────────────────────────────────────────────────
    @FXML private Label                              lblItemsHeader;
    @FXML private TableView<TransactionItem>         itemsTable;
    @FXML private TableColumn<TransactionItem,String> colItemName;
    @FXML private TableColumn<TransactionItem,String> colItemQty;
    @FXML private TableColumn<TransactionItem,String> colItemUnitPrice;
    @FXML private TableColumn<TransactionItem,String> colItemSubtotal;

    // ── Analytics ─────────────────────────────────────────────────────────
    @FXML private TableView<Object[]>          topTable;
    @FXML private TableColumn<Object[],String> colTopName;
    @FXML private TableColumn<Object[],String> colTopQty;
    @FXML private TableColumn<Object[],String> colTopRevenue;

    @FXML private Label lblCashCount;
    @FXML private Label lblCashTotal;
    @FXML private Label lblCardCount;
    @FXML private Label lblCardTotal;

    private final CashierReportService reportService;
    private final StageManager         stageManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dateFrom.setValue(LocalDate.now().withDayOfMonth(1));   // first of current month
        dateTo.setValue(LocalDate.now());

        setupTransactionTable();
        setupItemsTable();
        setupTopTable();
        loadData();
    }

    // ── Table setup ───────────────────────────────────────────────────────

    private void setupTransactionTable() {
        colTxId.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colTxDate.setCellValueFactory(c ->
                new SimpleStringProperty(DateUtil.format(c.getValue().getCreatedAt())));
        colTxCashier.setCellValueFactory(c -> {
            var cashier = c.getValue().getCashier();
            return new SimpleStringProperty(cashier != null ? cashier.getUsername() : "—");
        });
        colTxSubtotal.setCellValueFactory(c ->
                new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getSubtotal())));
        colTxDiscount.setCellValueFactory(c -> {
            BigDecimal d = c.getValue().getDiscountAmount();
            String text = (d != null && d.compareTo(BigDecimal.ZERO) > 0)
                    ? "-" + CurrencyFormatter.format(d) : "—";
            return new SimpleStringProperty(text);
        });
        colTxTotal.setCellValueFactory(c ->
                new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getTotal())));
        colTxMethod.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPaymentMethod().name()));

        // When a transaction is selected, populate the items detail table
        txTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, tx) -> showTransactionItems(tx));
    }

    private void setupItemsTable() {
        colItemName.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getProductName()));
        colItemQty.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colItemUnitPrice.setCellValueFactory(c ->
                new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getUnitPrice())));
        colItemSubtotal.setCellValueFactory(c ->
                new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getSubtotal())));
    }

    private void setupTopTable() {
        colTopName.setCellValueFactory(c ->
                new SimpleStringProperty((String) c.getValue()[0]));
        colTopQty.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue()[1])));
        colTopRevenue.setCellValueFactory(c ->
                new SimpleStringProperty(CurrencyFormatter.format((BigDecimal) c.getValue()[2])));
    }

    // ── Data loading ──────────────────────────────────────────────────────

    private void loadData() {
        LocalDate from = dateFrom.getValue();
        LocalDate to   = dateTo.getValue();

        if (from == null || to == null) return;
        if (from.isAfter(to)) {
            AlertHelper.showWarning("Invalid Range", "'From' date must be before 'To' date.");
            return;
        }

        // KPI cards
        RangeSummary summary = reportService.getRangeSummary(from, to);
        lblRevenue.setText(CurrencyFormatter.format(summary.totalRevenue()));
        lblTxCount.setText(String.valueOf(summary.txCount()));
        lblAvgOrder.setText(CurrencyFormatter.format(summary.avgOrderValue()));
        lblItemsSold.setText(String.valueOf(summary.itemsSold()));
        lblDiscounts.setText(CurrencyFormatter.format(summary.totalDiscount()));

        // Transaction history
        List<Transaction> history = reportService.getHistory(from, to);
        txTable.setItems(FXCollections.observableArrayList(history));
        itemsTable.getItems().clear();
        lblItemsHeader.setText("Items in selected transaction:");

        // Top products by revenue
        List<Object[]> top = reportService.getTopProductsByRevenue(10, from, to);
        topTable.setItems(FXCollections.observableArrayList(top));

        // Payment breakdown
        loadPaymentBreakdown(reportService.getPaymentBreakdown(from, to));
    }

    private void showTransactionItems(Transaction tx) {
        if (tx == null) {
            itemsTable.getItems().clear();
            lblItemsHeader.setText("Items in selected transaction:");
            return;
        }
        lblItemsHeader.setText("Items in transaction #" + tx.getId()
                + "  (" + tx.getItems().size() + " line"
                + (tx.getItems().size() == 1 ? "" : "s") + "):");
        itemsTable.setItems(FXCollections.observableArrayList(tx.getItems()));
    }

    private void loadPaymentBreakdown(List<Object[]> rows) {
        // Reset
        lblCashCount.setText("0");  lblCashTotal.setText("0.00");
        lblCardCount.setText("0");  lblCardTotal.setText("0.00");

        for (Object[] row : rows) {
            String method = row[0].toString();
            long   count  = ((Number) row[1]).longValue();
            BigDecimal total = (BigDecimal) row[2];
            if ("CASH".equals(method)) {
                lblCashCount.setText(String.valueOf(count));
                lblCashTotal.setText(CurrencyFormatter.format(total));
            } else if ("CARD".equals(method)) {
                lblCardCount.setText(String.valueOf(count));
                lblCardTotal.setText(CurrencyFormatter.format(total));
            }
        }
    }

    // ── FXML handlers ─────────────────────────────────────────────────────

    @FXML
    void onSearch() { loadData(); }

    @FXML
    void onBack() { stageManager.showCashierScreen(); }
}
