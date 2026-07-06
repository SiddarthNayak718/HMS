package com.hms.view;

import com.hms.dao.MedicineDAO;
import com.hms.model.Medicine;
import com.hms.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

public class MedicineView {

    private final MedicineDAO dao = new MedicineDAO();
    private TableView<Medicine> table;

    private TextField  tfName, tfCategory, tfPrice, tfStock, tfMfr;
    private DatePicker dpExpiry;
    private int editingId = -1;

    public Node getView() {
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.62);
        split.getItems().addAll(buildTablePane(), buildFormPane());
        refreshTable();
        return split;
    }

    private VBox buildTablePane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(16));

        Label title = new Label("Medicine Inventory");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        TextField search = new TextField();
        search.setPromptText("🔍 Search medicine…");
        search.setPrefWidth(240);
        search.setOnKeyReleased(e -> filterTable(search.getText()));

        Button btnLow = new Button("⚠ Low Stock");
        btnLow.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-background-radius:6;");
        btnLow.setOnAction(e -> {
            try { table.setItems(FXCollections.observableArrayList(dao.getLowStock(50))); }
            catch (Exception ex) { AlertUtil.showError("Error", ex.getMessage()); }
        });

        Button btnRefresh = new Button("↻");
        btnRefresh.setOnAction(e -> { search.clear(); refreshTable(); });

        HBox top = new HBox(8, search, btnLow, btnRefresh);
        top.setAlignment(Pos.CENTER_LEFT);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.getColumns().addAll(
            col("ID",          "medicineId",   55),
            col("Name",        "name",         180),
            col("Category",    "category",     110),
            col("Unit Price",  "unitPrice",     90),
            col("Stock Qty",   "stockQty",      80),
            col("Manufacturer","manufacturer", 130),
            col("Expiry",      "expiryDate",   100)
        );
        table.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, sel) -> { if (sel != null) populateForm(sel); });
        table.setPlaceholder(new Label("No medicines found."));

        pane.getChildren().addAll(title, top, table);
        return pane;
    }

    private VBox buildFormPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(16));

        Label title = new Label("Medicine Details");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        tfName     = field("Medicine Name *");
        tfCategory = field("Category");
        tfPrice    = field("Unit Price (₹) *");
        tfStock    = field("Stock Quantity *");
        tfMfr      = field("Manufacturer");
        dpExpiry   = new DatePicker();
        dpExpiry.setPromptText("Expiry Date");
        dpExpiry.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.addRow(0, lbl("Name"),         tfName);
        grid.addRow(1, lbl("Category"),     tfCategory);
        grid.addRow(2, lbl("Unit Price"),   tfPrice);
        grid.addRow(3, lbl("Stock Qty"),    tfStock);
        grid.addRow(4, lbl("Manufacturer"), tfMfr);
        grid.addRow(5, lbl("Expiry Date"),  dpExpiry);
        ColumnConstraints c1 = new ColumnConstraints(100);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        Button btnSave = new Button("💾 Save");
        btnSave.setStyle("-fx-background-color: #27ae60; -fx-text-fill:white; " +
                         "-fx-font-weight:bold; -fx-background-radius:6; -fx-padding:8 18;");
        btnSave.setOnAction(e -> saveMedicine());

        Button btnClear = new Button("✖ Clear");
        btnClear.setStyle("-fx-background-color:#95a5a6; -fx-text-fill:white; " +
                          "-fx-background-radius:6; -fx-padding:8 14;");
        btnClear.setOnAction(e -> clearForm());

        pane.getChildren().addAll(title, grid, new HBox(10, btnSave, btnClear));
        return pane;
    }

    private void saveMedicine() {
        if (tfName.getText().isBlank() || tfPrice.getText().isBlank() || tfStock.getText().isBlank()) {
            AlertUtil.showError("Validation", "Name, price, and stock quantity are required.");
            return;
        }
        double price; int stock;
        try {
            price = Double.parseDouble(tfPrice.getText().trim());
            stock = Integer.parseInt(tfStock.getText().trim());
        } catch (NumberFormatException ex) {
            AlertUtil.showError("Validation", "Price and stock must be valid numbers.");
            return;
        }

        Medicine m = new Medicine();
        m.setName(tfName.getText().trim());
        m.setCategory(tfCategory.getText().trim());
        m.setUnitPrice(price);
        m.setStockQty(stock);
        m.setManufacturer(tfMfr.getText().trim());
        m.setExpiryDate(dpExpiry.getValue());

        try {
            if (editingId == -1) {
                dao.insertMedicine(m);
                AlertUtil.showInfo("Success", "Medicine added successfully.");
            } else {
                m.setMedicineId(editingId);
                dao.updateMedicine(m);
                AlertUtil.showInfo("Success", "Medicine updated successfully.");
            }
            clearForm();
            refreshTable();
        } catch (Exception ex) {
            AlertUtil.showError("DB Error", ex.getMessage());
        }
    }

    private void populateForm(Medicine m) {
        editingId = m.getMedicineId();
        tfName.setText(m.getName());
        tfCategory.setText(m.getCategory() != null ? m.getCategory() : "");
        tfPrice.setText(String.valueOf(m.getUnitPrice()));
        tfStock.setText(String.valueOf(m.getStockQty()));
        tfMfr.setText(m.getManufacturer() != null ? m.getManufacturer() : "");
        dpExpiry.setValue(m.getExpiryDate());
    }

    private void clearForm() {
        editingId = -1;
        tfName.clear(); tfCategory.clear(); tfPrice.clear();
        tfStock.clear(); tfMfr.clear(); dpExpiry.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        try { table.setItems(FXCollections.observableArrayList(dao.getAllMedicines())); }
        catch (Exception e) { /* DB not available */ }
    }

    private void filterTable(String kw) {
        try {
            if (kw == null || kw.isBlank()) { refreshTable(); return; }
            List<Medicine> all = dao.getAllMedicines();
            String lk = kw.toLowerCase();
            table.setItems(FXCollections.observableArrayList(
                all.stream().filter(m -> m.getName().toLowerCase().contains(lk) ||
                    (m.getCategory() != null && m.getCategory().toLowerCase().contains(lk)))
                   .collect(java.util.stream.Collectors.toList())));
        } catch (Exception e) { /* ignore */ }
    }

    private TextField field(String p) { TextField tf = new TextField(); tf.setPromptText(p); return tf; }
    private Label     lbl(String t)   { Label l = new Label(t); l.setStyle("-fx-font-weight:bold;"); return l; }

    @SuppressWarnings("unchecked")
    private TableColumn col(String h, String prop, int min) {
        TableColumn tc = new TableColumn(h);
        tc.setCellValueFactory(new PropertyValueFactory<>(prop));
        tc.setMinWidth(min);
        return tc;
    }
}
