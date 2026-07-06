package com.hms.view;

import com.hms.dao.*;
import com.hms.model.*;
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

public class ReportsView {

    private final AppointmentDAO apptDAO    = new AppointmentDAO();
    private final PatientDAO     patientDAO = new PatientDAO();
    private final BillDAO        billDAO    = new BillDAO();
    private final MedicineDAO    medDAO     = new MedicineDAO();

    public Node getView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));

        Label title = new Label("Reports & Analytics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        tabs.getTabs().addAll(
            buildAppointmentsTab(),
            buildPatientHistoryTab(),
            buildRevenueTab(),
            buildLowStockTab()
        );

        root.getChildren().addAll(title, tabs);
        return root;
    }

    // -------------------------------------------------------
    // TAB 1 – Daily Appointments
    // -------------------------------------------------------
    private Tab buildAppointmentsTab() {
        Tab tab = new Tab("📅 Daily Appointments");
        VBox pane = new VBox(10); pane.setPadding(new Insets(12));

        DatePicker dp = new DatePicker(LocalDate.now());
        Button btnLoad = new Button("Load");
        btnLoad.setStyle("-fx-background-color:#2980b9; -fx-text-fill:white; -fx-background-radius:6;");

        HBox bar = new HBox(8, new Label("Date:"), dp, btnLoad);
        bar.setAlignment(Pos.CENTER_LEFT);

        TableView<Appointment> t = buildApptTable();
        VBox.setVgrow(t, Priority.ALWAYS);

        btnLoad.setOnAction(e -> {
            // load all, filter by date client-side for simplicity
            try {
                List<Appointment> all = apptDAO.getAllAppointments();
                LocalDate sel = dp.getValue();
                t.setItems(FXCollections.observableArrayList(
                    all.stream().filter(a -> sel.equals(a.getApptDate()))
                       .collect(java.util.stream.Collectors.toList())));
            } catch (Exception ex) { AlertUtil.showError("Error", ex.getMessage()); }
        });

        // auto-load today
        try {
            t.setItems(FXCollections.observableArrayList(apptDAO.getTodaysAppointments()));
        } catch (Exception e) { /* ignore */ }

        pane.getChildren().addAll(bar, t);
        tab.setContent(pane);
        return tab;
    }

    // -------------------------------------------------------
    // TAB 2 – Patient History
    // -------------------------------------------------------
    private Tab buildPatientHistoryTab() {
        Tab tab = new Tab("👥 Patient Appointments");
        VBox pane = new VBox(10); pane.setPadding(new Insets(12));

        ComboBox<Patient> cbPat = new ComboBox<>();
        try { cbPat.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients())); }
        catch (Exception e) { /* ignore */ }
        cbPat.setPromptText("Select Patient");
        cbPat.setPrefWidth(280);

        Button btnLoad = new Button("Load History");
        btnLoad.setStyle("-fx-background-color:#8e44ad; -fx-text-fill:white; -fx-background-radius:6;");

        HBox bar = new HBox(8, new Label("Patient:"), cbPat, btnLoad);
        bar.setAlignment(Pos.CENTER_LEFT);

        TableView<Appointment> t = buildApptTable();
        VBox.setVgrow(t, Priority.ALWAYS);

        btnLoad.setOnAction(e -> {
            if (cbPat.getValue() == null) return;
            try {
                t.setItems(FXCollections.observableArrayList(
                    apptDAO.getByPatient(cbPat.getValue().getPatientId())));
            } catch (Exception ex) { AlertUtil.showError("Error", ex.getMessage()); }
        });

        pane.getChildren().addAll(bar, t);
        tab.setContent(pane);
        return tab;
    }

    // -------------------------------------------------------
    // TAB 3 – Revenue Summary
    // -------------------------------------------------------
    private Tab buildRevenueTab() {
        Tab tab = new Tab("💰 Revenue Summary");
        VBox pane = new VBox(10); pane.setPadding(new Insets(12));

        Label todayRev = new Label();
        todayRev.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#27ae60;");
        try {
            double rev = billDAO.getTodaysRevenue();
            todayRev.setText("Today's Revenue: ₹" + String.format("%.2f", rev));
        } catch (Exception e) {
            todayRev.setText("Today's Revenue: N/A");
        }

        TableView<Bill> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(t, Priority.ALWAYS);
        t.getColumns().addAll(
            bcol("Bill ID",    "billId",         60),
            bcol("Patient",    "patientName",    160),
            bcol("Date",       "billDate",        90),
            bcol("Total (₹)",  "totalAmount",     90),
            bcol("Discount",   "discount",         80),
            bcol("Net (₹)",    "netAmount",        90),
            bcol("Status",     "status",           70)
        );

        try { t.setItems(FXCollections.observableArrayList(billDAO.getAllBills())); }
        catch (Exception e) { /* ignore */ }

        pane.getChildren().addAll(todayRev, t);
        tab.setContent(pane);
        return tab;
    }

    // -------------------------------------------------------
    // TAB 4 – Low Stock Alert
    // -------------------------------------------------------
    private Tab buildLowStockTab() {
        Tab tab = new Tab("⚠ Low Stock");
        VBox pane = new VBox(10); pane.setPadding(new Insets(12));

        Label info = new Label("Medicines with stock ≤ 50 units:");
        info.setStyle("-fx-font-size:14px; -fx-text-fill:#e67e22;");

        TableView<Medicine> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(t, Priority.ALWAYS);
        t.getColumns().addAll(
            mcol("ID",          "medicineId",   55),
            mcol("Name",        "name",         200),
            mcol("Category",    "category",     120),
            mcol("Stock",       "stockQty",      80),
            mcol("Manufacturer","manufacturer", 150)
        );

        try { t.setItems(FXCollections.observableArrayList(medDAO.getLowStock(50))); }
        catch (Exception e) { /* ignore */ }
        t.setPlaceholder(new Label("✅ All medicines have adequate stock."));

        pane.getChildren().addAll(info, t);
        tab.setContent(pane);
        return tab;
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------
    @SuppressWarnings("unchecked")
    private TableView<Appointment> buildApptTable() {
        TableView<Appointment> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.getColumns().addAll(
            acol("ID",      "apptId",      55),
            acol("Patient", "patientName", 160),
            acol("Doctor",  "doctorName",  160),
            acol("Date",    "apptDate",    100),
            acol("Time",    "apptTime",     70),
            acol("Reason",  "reason",      180),
            acol("Status",  "status",       90)
        );
        return t;
    }

    @SuppressWarnings("unchecked")
    private TableColumn acol(String h, String p, int min) {
        TableColumn tc = new TableColumn(h);
        tc.setCellValueFactory(new PropertyValueFactory<>(p));
        tc.setMinWidth(min); return tc;
    }
    @SuppressWarnings("unchecked")
    private TableColumn bcol(String h, String p, int min) {
        TableColumn tc = new TableColumn(h);
        tc.setCellValueFactory(new PropertyValueFactory<>(p));
        tc.setMinWidth(min); return tc;
    }
    @SuppressWarnings("unchecked")
    private TableColumn mcol(String h, String p, int min) {
        TableColumn tc = new TableColumn(h);
        tc.setCellValueFactory(new PropertyValueFactory<>(p));
        tc.setMinWidth(min); return tc;
    }
}
