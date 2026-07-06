package com.hms.view;

import com.hms.dao.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class DashboardView {

    private final PatientDAO     patientDAO     = new PatientDAO();
    private final DoctorDAO      doctorDAO      = new DoctorDAO();
    private final AppointmentDAO apptDAO        = new AppointmentDAO();
    private final BillDAO        billDAO        = new BillDAO();

    public Node getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        // Stat cards
        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER_LEFT);

        int patients  = safeCount(() -> patientDAO.getTotalPatients());
        int doctors   = safeCount(() -> doctorDAO.getTotalDoctors());
        int appts     = safeCount(() -> apptDAO.getTodaysCount());
        double revenue = safeRevenue();

        cards.getChildren().addAll(
            card("👥 Total Patients",  String.valueOf(patients),  "#2980b9"),
            card("🩺 Active Doctors",  String.valueOf(doctors),   "#27ae60"),
            card("📅 Today Appts",     String.valueOf(appts),     "#e67e22"),
            card("💰 Today Revenue",   "₹" + String.format("%.2f", revenue), "#8e44ad")
        );

        // Today's appointment table
        Label tblTitle = new Label("Today's Appointments");
        tblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        TableView<?> table = buildTodayTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(title, cards, tblTitle, table);
        return root;
    }

    private VBox card(String label, String value, String color) {
        VBox card = new VBox(6);
        card.setPrefWidth(200);
        card.setPrefHeight(90);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + color + "; " +
                      "-fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.15),8,0,2,2);");

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.9);");
        card.getChildren().addAll(val, lbl);
        return card;
    }

    @SuppressWarnings("unchecked")
    private TableView buildTodayTable() {
        TableView table = new TableView();
        table.setMaxHeight(320);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<?,?> id     = col("Appt ID",      "apptId",      60);
        TableColumn<?,?> pat    = col("Patient",       "patientName", 200);
        TableColumn<?,?> doc    = col("Doctor",        "doctorName",  200);
        TableColumn<?,?> time   = col("Time",          "apptTime",    80);
        TableColumn<?,?> reason = col("Reason",        "reason",      200);
        TableColumn<?,?> status = col("Status",        "status",      100);

        table.getColumns().addAll(id, pat, doc, time, reason, status);

        try {
            table.getItems().addAll(new AppointmentDAO().getTodaysAppointments());
        } catch (Exception e) {
            // DB not connected — show placeholder
        }

        table.setPlaceholder(new Label("No appointments today."));
        return table;
    }

    private TableColumn col(String heading, String prop, int minW) {
        TableColumn tc = new TableColumn(heading);
        tc.setCellValueFactory(new PropertyValueFactory<>(prop));
        tc.setMinWidth(minW);
        return tc;
    }

    private int safeCount(java.util.concurrent.Callable<Integer> fn) {
        try { return fn.call(); } catch (Exception e) { return 0; }
    }
    private double safeRevenue() {
        try { return billDAO.getTodaysRevenue(); } catch (Exception e) { return 0; }
    }
}
