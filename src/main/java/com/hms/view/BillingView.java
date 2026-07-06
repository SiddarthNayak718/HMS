package com.hms.view;

import com.hms.dao.BillDAO;
import com.hms.dao.PatientDAO;
import com.hms.model.Bill;
import com.hms.model.Patient;
import com.hms.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class BillingView {

    private final BillDAO    billDAO    = new BillDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    private TableView<Bill> table;

    private ComboBox<Patient> cbPatient;
    private TextField  tfConsult, tfMedicine, tfLab, tfDiscount;
    private ComboBox<String> cbPayMode;
    private TextField  tfPayAmount;
    private Label      lblTotal, lblNet;

    public Node getView() {
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.60);
        split.getItems().addAll(buildTablePane(), buildFormPane());
        refreshTable();
        return split;
    }

    // -------------------------------------------------------
    private VBox buildTablePane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(16));

        Label title = new Label("Billing & Payments");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.setOnAction(e -> refreshTable());

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.getColumns().addAll(
            col("Bill ID",     "billId",          60),
            col("Patient",     "patientName",     160),
            col("Date",        "billDate",         90),
            col("Consult(₹)",  "consultationFee",  90),
            col("Meds(₹)",     "medicineAmount",   90),
            col("Lab(₹)",      "labAmount",         80),
            col("Total(₹)",    "totalAmount",      90),
            col("Discount",    "discount",          70),
            col("Net(₹)",      "netAmount",         90),
            col("Status",      "status",            70)
        );
        table.setPlaceholder(new Label("No bills found."));

        // Record payment button
        Button btnPay = new Button("💳 Record Payment");
        btnPay.setStyle("-fx-background-color: #2980b9; -fx-text-fill:white; " +
                        "-fx-font-weight:bold; -fx-background-radius:6;");
        btnPay.setOnAction(e -> recordPaymentForSelected());

        HBox bottom = new HBox(8, btnRefresh, btnPay);
        pane.getChildren().addAll(title, table, bottom);
        return pane;
    }

    // -------------------------------------------------------
    private VBox buildFormPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(16));

        Label title = new Label("Generate Bill");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        List<Patient> patients = loadPatients();
        cbPatient = new ComboBox<>(FXCollections.observableArrayList(patients));
        cbPatient.setPromptText("Select Patient *");
        cbPatient.setMaxWidth(Double.MAX_VALUE);

        tfConsult  = numField("Consultation Fee (₹)");
        tfMedicine = numField("Medicine Amount (₹)");
        tfLab      = numField("Lab Amount (₹)");
        tfDiscount = numField("Discount (₹)");

        // Live total calculation
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> calc = e -> recalcTotal();
        tfConsult.setOnKeyReleased(calc);
        tfMedicine.setOnKeyReleased(calc);
        tfLab.setOnKeyReleased(calc);
        tfDiscount.setOnKeyReleased(calc);

        lblTotal = new Label("Total: ₹0.00");
        lblTotal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblNet   = new Label("Net Payable: ₹0.00");
        lblNet.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Separator sep = new Separator();

        // Payment section
        Label payLbl = new Label("Record Payment");
        payLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        tfPayAmount = numField("Amount to Pay (₹)");
        cbPayMode   = new ComboBox<>(FXCollections.observableArrayList(
            "CASH","CARD","UPI","INSURANCE","ONLINE"));
        cbPayMode.setPromptText("Payment Mode");
        cbPayMode.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.addRow(0, lbl("Patient"),     cbPatient);
        grid.addRow(1, lbl("Consult Fee"), tfConsult);
        grid.addRow(2, lbl("Medicines"),   tfMedicine);
        grid.addRow(3, lbl("Lab Tests"),   tfLab);
        grid.addRow(4, lbl("Discount"),    tfDiscount);
        grid.addRow(5, new Label(""),      lblTotal);
        grid.addRow(6, new Label(""),      lblNet);
        grid.addRow(7, new Label(""),      sep);
        grid.addRow(8, lbl("Pay Amount"),  tfPayAmount);
        grid.addRow(9, lbl("Pay Mode"),    cbPayMode);

        ColumnConstraints c1 = new ColumnConstraints(100);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        Button btnGenerate = new Button("🧾 Generate Bill");
        btnGenerate.setStyle("-fx-background-color: #27ae60; -fx-text-fill:white; " +
                             "-fx-font-weight:bold; -fx-background-radius:6; -fx-padding:8 14;");
        btnGenerate.setOnAction(e -> generateBill());

        Button btnClear = new Button("✖ Clear");
        btnClear.setStyle("-fx-background-color:#95a5a6; -fx-text-fill:white; " +
                          "-fx-background-radius:6; -fx-padding:8 12;");
        btnClear.setOnAction(e -> clearForm());

        pane.getChildren().addAll(title, grid, new HBox(10, btnGenerate, btnClear));
        return pane;
    }

    // -------------------------------------------------------
    private void recalcTotal() {
        double c = parseNum(tfConsult);
        double m = parseNum(tfMedicine);
        double l = parseNum(tfLab);
        double d = parseNum(tfDiscount);
        double total = c + m + l;
        double net   = total - d;
        lblTotal.setText(String.format("Total: ₹%.2f", total));
        lblNet.setText(String.format("Net Payable: ₹%.2f", net));
    }

    private void generateBill() {
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

            // If payment details provided, record immediately
            double payAmt = parseNum(tfPayAmount);
            if (payAmt > 0 && cbPayMode.getValue() != null) {
                // Get last bill id
                List<Bill> bills = billDAO.getAllBills();
                if (!bills.isEmpty()) {
                    int lastBillId = bills.get(0).getBillId();
                    billDAO.recordPayment(lastBillId, payAmt, cbPayMode.getValue());
                }
            }

            AlertUtil.showInfo("Success", "Bill generated successfully.");
            clearForm();
            refreshTable();
        } catch (Exception ex) {
            AlertUtil.showError("DB Error", ex.getMessage());
        }
    }

    private void recordPaymentForSelected() {
        Bill sel = table.getSelectionModel().getSelectedItem();
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
        TextField amt  = new TextField();  amt.setPromptText("Amount");
        ComboBox<String> mode = new ComboBox<>(FXCollections.observableArrayList(
            "CASH","CARD","UPI","INSURANCE","ONLINE"));
        mode.setValue("CASH");
        g.addRow(0, new Label("Amount (₹):"), amt);
        g.addRow(1, new Label("Mode:"),       mode);
        g.addRow(2, new Label("Net Due:"), new Label("₹" + String.format("%.2f", sel.getNetAmount())));
        dlg.getDialogPane().setContent(g);

        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    double a = Double.parseDouble(amt.getText().trim());
                    billDAO.recordPayment(sel.getBillId(), a, mode.getValue());
                    AlertUtil.showInfo("Success", "Payment recorded.");
                    refreshTable();
                } catch (Exception ex) {
                    AlertUtil.showError("Error", ex.getMessage());
                }
            }
        });
    }

    private void clearForm() {
        cbPatient.setValue(null);
        tfConsult.clear(); tfMedicine.clear(); tfLab.clear(); tfDiscount.clear();
        tfPayAmount.clear(); cbPayMode.setValue(null);
        lblTotal.setText("Total: ₹0.00"); lblNet.setText("Net Payable: ₹0.00");
    }

    private void refreshTable() {
        try { table.setItems(FXCollections.observableArrayList(billDAO.getAllBills())); }
        catch (Exception e) { /* DB not available */ }
    }

    private List<Patient> loadPatients() {
        try { return patientDAO.getAllPatients(); }
        catch (Exception e) { return java.util.Collections.emptyList(); }
    }

    private double parseNum(TextField tf) {
        try { return Double.parseDouble(tf.getText().trim()); }
        catch (Exception e) { return 0.0; }
    }

    private TextField numField(String p) {
        TextField tf = new TextField(); tf.setPromptText(p); return tf;
    }
    private Label lbl(String t) {
        Label l = new Label(t); l.setStyle("-fx-font-weight:bold;"); return l;
    }
    @SuppressWarnings("unchecked")
    private TableColumn col(String h, String prop, int min) {
        TableColumn tc = new TableColumn(h);
        tc.setCellValueFactory(new PropertyValueFactory<>(prop));
        tc.setMinWidth(min);
        return tc;
    }
}
