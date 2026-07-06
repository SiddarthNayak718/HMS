package com.hms.controller;

import com.hms.dao.MedicineDAO;
import com.hms.model.Medicine;
import com.hms.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MedicineController implements Initializable {

    // Table
    @FXML private TableView<Medicine>            medicineTable;
    @FXML private TableColumn<Medicine, Integer> colId;
    @FXML private TableColumn<Medicine, String>  colName;
    @FXML private TableColumn<Medicine, String>  colCat;
    @FXML private TableColumn<Medicine, Double>  colPrice;
    @FXML private TableColumn<Medicine, Integer> colStock;
    @FXML private TableColumn<Medicine, String>  colMfr;
    @FXML private TableColumn<Medicine, LocalDate> colExp;

    // Form
    @FXML private TextField  tfName;
    @FXML private TextField  tfCategory;
    @FXML private TextField  tfPrice;
    @FXML private TextField  tfStock;
    @FXML private TextField  tfMfr;
    @FXML private DatePicker dpExpiry;

    // Search
    @FXML private TextField searchField;

    private final MedicineDAO dao = new MedicineDAO();
    private int editingId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("medicineId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQty"));
        colMfr.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        colExp.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        medicineTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        medicineTable.setPlaceholder(new Label("No medicines found."));

        medicineTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, sel) -> { if (sel != null) populateForm(sel); });

        refreshTable();
    }

    @FXML private void onSearch() { filterTable(searchField.getText()); }

    @FXML private void onLowStock() {
        try { medicineTable.setItems(FXCollections.observableArrayList(dao.getLowStock(50))); }
        catch (Exception ex) { AlertUtil.showError("Error", ex.getMessage()); }
    }

    @FXML private void onRefresh() { searchField.clear(); refreshTable(); }

    @FXML private void onSave() {
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
            onClear();
            refreshTable();
        } catch (Exception ex) {
            AlertUtil.showError("DB Error", ex.getMessage());
        }
    }

    @FXML private void onClear() {
        editingId = -1;
        tfName.clear(); tfCategory.clear(); tfPrice.clear();
        tfStock.clear(); tfMfr.clear(); dpExpiry.setValue(null);
        medicineTable.getSelectionModel().clearSelection();
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

    private void refreshTable() {
        try { medicineTable.setItems(FXCollections.observableArrayList(dao.getAllMedicines())); }
        catch (Exception e) { /* DB not available */ }
    }

    private void filterTable(String kw) {
        try {
            if (kw == null || kw.isBlank()) { refreshTable(); return; }
            String lk = kw.toLowerCase();
            List<Medicine> all = dao.getAllMedicines();
            medicineTable.setItems(FXCollections.observableArrayList(
                all.stream().filter(m -> m.getName().toLowerCase().contains(lk) ||
                    (m.getCategory() != null && m.getCategory().toLowerCase().contains(lk)))
                   .collect(Collectors.toList())));
        } catch (Exception e) { /* ignore */ }
    }
}
