package com.hms.controller;

import com.hms.dao.AppointmentDAO;
import com.hms.dao.BillDAO;
import com.hms.dao.DoctorDAO;
import com.hms.dao.PatientDAO;
import com.hms.model.Appointment;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private HBox cardsBox;

    @FXML private TableView<Appointment> todayTable;
    @FXML private TableColumn<Appointment, Integer> colApptId;
    @FXML private TableColumn<Appointment, String>  colPatientName;
    @FXML private TableColumn<Appointment, String>  colDoctorName;
    @FXML private TableColumn<Appointment, String>  colApptTime;
    @FXML private TableColumn<Appointment, String>  colReason;
    @FXML private TableColumn<Appointment, String>  colStatus;

    private final PatientDAO     patientDAO = new PatientDAO();
    private final DoctorDAO      doctorDAO  = new DoctorDAO();
    private final AppointmentDAO apptDAO    = new AppointmentDAO();
    private final BillDAO        billDAO    = new BillDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind columns
        colApptId.setCellValueFactory(new PropertyValueFactory<>("apptId"));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctorName.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colApptTime.setCellValueFactory(new PropertyValueFactory<>("apptTime"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        todayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        todayTable.setPlaceholder(new Label("No appointments today."));

        buildCards();
        loadTodayAppointments();
    }

    private void buildCards() {
        int patients = safeCount(() -> patientDAO.getTotalPatients());
        int doctors  = safeCount(() -> doctorDAO.getTotalDoctors());
        int appts    = safeCount(() -> apptDAO.getTodaysCount());
        double revenue = safeRevenue();

        cardsBox.getChildren().addAll(
            card("👥 Total Patients", String.valueOf(patients),          "#2980b9"),
            card("🩺 Active Doctors", String.valueOf(doctors),           "#27ae60"),
            card("📅 Today Appts",    String.valueOf(appts),             "#e67e22"),
            card("💰 Today Revenue",  "₹" + String.format("%.2f", revenue), "#8e44ad")
        );
    }

    private VBox card(String labelText, String value, String color) {
        VBox card = new VBox(6);
        card.setPrefWidth(200);
        card.setPrefHeight(90);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.15),8,0,2,2);");

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.9);");
        card.getChildren().addAll(val, lbl);
        return card;
    }

    private void loadTodayAppointments() {
        try {
            todayTable.setItems(
                FXCollections.observableArrayList(apptDAO.getTodaysAppointments()));
        } catch (Exception e) { /* DB not connected */ }
    }

    private int safeCount(java.util.concurrent.Callable<Integer> fn) {
        try { return fn.call(); } catch (Exception e) { return 0; }
    }

    private double safeRevenue() {
        try { return billDAO.getTodaysRevenue(); } catch (Exception e) { return 0; }
    }
}
