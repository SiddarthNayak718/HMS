package com.hms.controller;

import com.hms.dao.BillDAO;
import com.hms.dao.PatientDAO;
import com.hms.model.Bill;
import com.hms.model.Patient;
import com.hms.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BillingController implements Initializable {

    // Table
    @FXML private TableView<Bill>            billTable;
    @FXML private TableColumn<Bill, Integer> colBillId;
    @FXML private TableColumn<Bill, String>  colBillPat;
    @FXML private TableColumn<Bill, ?>       colBillDate;
    @FXML private TableColumn<Bill, Double>  colConsult;
    @FXML private TableColumn<Bill, Double>  colMeds;
    @FXML private TableColumn<Bill, Double>  colLab;
    @FXML private TableColumn<Bill, Double>  colTotal;
    @FXML private TableColumn<Bill, Double>  colDiscount;
    @FXML private TableColumn<Bill, Double>  colNet;
    @FXML private TableColumn<Bill, String>  colBStatus;

    // Form
    @FXML private ComboBox<Patient> cbPatient;
    @FXML private TextField  tfConsult;
    @FXML private TextField  tfMedicine;
    @FXML private TextField  tfLab;
    @FXML private TextField  tfDiscount;
    @FXML private Label      lblTotal;
    @FXML private Label      lblNet;
    @FXML private TextField  tfPayAmount;
    @FXML private ComboBox<String> cbPayMode;

    private final BillDAO    billDAO    = new BillDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colBillId.setCellValueFactory(new PropertyValueFactory<>("billId"));
        colBillPat.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colBillDate.setCellValueFactory(new PropertyValueFactory<>("billDate"));
        colConsult.setCellValueFactory(new PropertyValueFactory<>("consultationFee"));
        colMeds.setCellValueFactory(new PropertyValueFactory<>("medicineAmount"));
        colLab.setCellValueFactory(new PropertyValueFactory<>("labAmount"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colNet.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        colBStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        billTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        billTable.setPlaceholder(new Label("No bills found."));

        try { cbPatient.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients())); }
        catch (Exception e) { /* ignore */ }

        cbPayMode.setItems(FXCollections.observableArrayList("CASH","CARD","UPI","INSURANCE","ONLINE"));

        // Live total recalculation
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> calc = e -> recalcTotal();
        tfConsult.setOnKeyReleased(calc);
        tfMedicine.setOnKeyReleased(calc);
        tfLab.setOnKeyReleased(calc);
        tfDiscount.setOnKeyReleased(calc);

        refreshTable();
    }

    @FXML private void onRefresh() { refreshTable(); }

    @FXML private void onGenerate() {
        if (cbPatient.getValue() == null) {
            AlertUtil.showError("Validation", "Please select a patient.");
            return;
        }
        Bill b = new Bill();
        b.setPatientId(cbPatient.getValue().getPatientId());
        b.setConsultationFee(parseNum(tfConsult));
        b.setMedicineAmount(parseNum(tfMedicine));
        b.setLabAmount(parseNum(tfLab));
        b.setDiscount(parseNum(tfDiscount));

        try {
            billDAO.insertBill(b);
            double payAmt = parseNum(tfPayAmount);
            if (payAmt > 0 && cbPayMode.getValue() != null) {
                List<Bill> bills = billDAO.getAllBills();
                if (!bills.isEmpty()) {
                    billDAO.recordPayment(bills.get(0).getBillId(), payAmt, cbPayMode.getValue());
                }
            }
            AlertUtil.showInfo("Success", "Bill generated successfully.");
            onClear();
            refreshTable();
        } catch (Exception ex) {
            AlertUtil.showError("DB Error", ex.getMessage());
        }
    }

    @FXML private void onRecordPayment() {
        Bill sel = billTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.showError("Select", "Select a bill from the table."); return; }
        if ("PAID".equals(sel.getStatus())) {
            AlertUtil.showInfo("Info", "This bill is already fully paid.");
            return;
        }

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Record Payment — Bill #" + sel.getBillId());
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10); g.setPadding(new Insets(16));
        TextField amt = new TextField(); amt.setPromptText("Amount");
        ComboBox<String> mode = new ComboBox<>(
            FXCollections.observableArrayList("CASH","CARD","UPI","INSURANCE","ONLINE"));
        mode.setValue("CASH");
        g.addRow(0, new Label("Amount (₹):"), amt);
        g.addRow(1, new Label("Mode:"),       mode);
        g.addRow(2, new Label("Net Due:"),    new Label("₹" + String.format("%.2f", sel.getNetAmount())));
        dlg.getDialogPane().setContent(g);

        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    billDAO.recordPayment(sel.getBillId(),
                        Double.parseDouble(amt.getText().trim()), mode.getValue());
                    AlertUtil.showInfo("Success", "Payment recorded.");
                    refreshTable();
                } catch (Exception ex) {
                    AlertUtil.showError("Error", ex.getMessage());
                }
            }
        });
    }

    @FXML private void onClear() {
        cbPatient.setValue(null);
        tfConsult.clear(); tfMedicine.clear(); tfLab.clear(); tfDiscount.clear();
        tfPayAmount.clear(); cbPayMode.setValue(null);
        lblTotal.setText("Total: ₹0.00"); lblNet.setText("Net Payable: ₹0.00");
    }

    private void recalcTotal() {
        double c = parseNum(tfConsult), m = parseNum(tfMedicine),
               l = parseNum(tfLab),   d = parseNum(tfDiscount);
        double total = c + m + l, net = total - d;
        lblTotal.setText(String.format("Total: ₹%.2f", total));
        lblNet.setText(String.format("Net Payable: ₹%.2f", net));
    }

    private void refreshTable() {
        try { billTable.setItems(FXCollections.observableArrayList(billDAO.getAllBills())); }
        catch (Exception e) { /* DB not available */ }
    }

    private double parseNum(TextField tf) {
        try { return Double.parseDouble(tf.getText().trim()); } catch (Exception e) { return 0.0; }
    }
}
