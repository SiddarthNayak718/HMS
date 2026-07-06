package com.hms.view;

import com.hms.dao.AppointmentDAO;
import com.hms.dao.DoctorDAO;
import com.hms.dao.PatientDAO;
import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
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

public class AppointmentView {

    private final AppointmentDAO apptDAO    = new AppointmentDAO();
    private final PatientDAO     patientDAO = new PatientDAO();
    private final DoctorDAO      doctorDAO  = new DoctorDAO();

    private TableView<Appointment> table;

    private ComboBox<Patient>  cbPatient;
    private ComboBox<Doctor>   cbDoctor;
    private DatePicker         dpDate;
    private ComboBox<String>   cbTime;
    private TextArea           taReason;

    public Node getView() {
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.62);
        split.getItems().addAll(buildTablePane(), buildFormPane());
        refreshTable();
        return split;
    }

    // -------------------------------------------------------
    private VBox buildTablePane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(16));

        Label title = new Label("Appointment Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        // Filter buttons
        Button btnAll   = new Button("All");
        Button btnToday = new Button("Today");
        Button btnRefresh = new Button("↻");

        btnAll.setOnAction(e -> {
            try { table.setItems(FXCollections.observableArrayList(apptDAO.getAllAppointments())); }
            catch (Exception ex) { AlertUtil.showError("Error", ex.getMessage()); }
        });
        btnToday.setOnAction(e -> {
            try { table.setItems(FXCollections.observableArrayList(apptDAO.getTodaysAppointments())); }
            catch (Exception ex) { AlertUtil.showError("Error", ex.getMessage()); }
        });
        btnRefresh.setOnAction(e -> refreshTable());

        HBox top = new HBox(6, btnAll, btnToday, btnRefresh);
        top.setAlignment(Pos.CENTER_LEFT);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.getColumns().addAll(
            col("ID",       "apptId",      55),
            col("Patient",  "patientName", 170),
            col("Doctor",   "doctorName",  170),
            col("Date",     "apptDate",    100),
            col("Time",     "apptTime",     70),
            col("Reason",   "reason",      180),
            col("Status",   "status",       90)
        );
        table.setPlaceholder(new Label("No appointments found."));

        // Action buttons row
        Button btnCancel = new Button("✖ Cancel");
        Button btnComplete = new Button("✔ Complete");
        btnCancel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill:white; -fx-background-radius:6;");
        btnComplete.setStyle("-fx-background-color: #27ae60; -fx-text-fill:white; -fx-background-radius:6;");

        btnCancel.setOnAction(e -> updateStatus("CANCELLED"));
        btnComplete.setOnAction(e -> updateStatus("COMPLETED"));

        HBox actions = new HBox(8, btnCancel, btnComplete);

        pane.getChildren().addAll(title, top, table, actions);
        return pane;
    }

    // -------------------------------------------------------
    private VBox buildFormPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(16));

        Label title = new Label("Book Appointment");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        List<Patient> patients = loadPatients();
        List<Doctor>  doctors  = loadDoctors();

        cbPatient = new ComboBox<>(FXCollections.observableArrayList(patients));
        cbPatient.setPromptText("Select Patient *");
        cbPatient.setMaxWidth(Double.MAX_VALUE);

        cbDoctor = new ComboBox<>(FXCollections.observableArrayList(doctors));
        cbDoctor.setPromptText("Select Doctor *");
        cbDoctor.setMaxWidth(Double.MAX_VALUE);

        dpDate = new DatePicker(LocalDate.now());
        dpDate.setMaxWidth(Double.MAX_VALUE);

        cbTime = new ComboBox<>(FXCollections.observableArrayList(
            "09:00","09:30","10:00","10:30","11:00","11:30",
            "12:00","14:00","14:30","15:00","15:30","16:00","16:30","17:00"
        ));
        cbTime.setPromptText("Select Time *");
        cbTime.setMaxWidth(Double.MAX_VALUE);

        taReason = new TextArea();
        taReason.setPromptText("Reason / Chief Complaint");
        taReason.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.addRow(0, lbl("Patient"),  cbPatient);
        grid.addRow(1, lbl("Doctor"),   cbDoctor);
        grid.addRow(2, lbl("Date"),     dpDate);
        grid.addRow(3, lbl("Time"),     cbTime);
        grid.addRow(4, lbl("Reason"),   taReason);
        ColumnConstraints c1 = new ColumnConstraints(90);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        Button btnBook  = new Button("📅 Book");
        btnBook.setStyle("-fx-background-color: #2980b9; -fx-text-fill:white; " +
                         "-fx-font-weight:bold; -fx-background-radius:6; -fx-padding:8 18;");
        btnBook.setOnAction(e -> bookAppointment());

        Button btnClear = new Button("✖ Clear");
        btnClear.setStyle("-fx-background-color:#95a5a6; -fx-text-fill:white; " +
                          "-fx-background-radius:6; -fx-padding:8 14;");
        btnClear.setOnAction(e -> clearForm());

        pane.getChildren().addAll(title, grid, new HBox(10, btnBook, btnClear));
        return pane;
    }

    // -------------------------------------------------------
    private void bookAppointment() {
        if (cbPatient.getValue() == null || cbDoctor.getValue() == null ||
            dpDate.getValue() == null || cbTime.getValue() == null) {
            AlertUtil.showError("Validation", "Patient, Doctor, Date, and Time are required.");
            return;
        }
        if (dpDate.getValue().isBefore(LocalDate.now())) {
            AlertUtil.showError("Validation", "Cannot book appointments in the past.");
            return;
        }
        Appointment a = new Appointment();
        a.setPatientId(cbPatient.getValue().getPatientId());
        a.setDoctorId(cbDoctor.getValue().getDoctorId());
        a.setApptDate(dpDate.getValue());
        a.setApptTime(cbTime.getValue());
        a.setReason(taReason.getText().trim());

        try {
            apptDAO.insertAppointment(a);
            AlertUtil.showInfo("Success", "Appointment booked successfully.");
            clearForm();
            refreshTable();
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("UQ_APPT"))
                AlertUtil.showError("Conflict", "Doctor already has an appointment at that time.");
            else AlertUtil.showError("DB Error", ex.getMessage());
        }
    }

    private void updateStatus(String status) {
        Appointment sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.showError("Select", "Please select an appointment."); return; }
        if (AlertUtil.showConfirm("Confirm", "Mark as " + status + "?")) {
            apptDAO.updateStatus(sel.getApptId(), status);
            refreshTable();
        }
    }

    private void clearForm() {
        cbPatient.setValue(null); cbDoctor.setValue(null);
        dpDate.setValue(LocalDate.now()); cbTime.setValue(null);
        taReason.clear();
    }

    private void refreshTable() {
        try { table.setItems(FXCollections.observableArrayList(apptDAO.getAllAppointments())); }
        catch (Exception e) { /* DB not available */ }
    }

    private List<Patient> loadPatients() {
        try { return patientDAO.getAllPatients(); }
        catch (Exception e) { return java.util.Collections.emptyList(); }
    }

    private List<Doctor> loadDoctors() {
        try { return doctorDAO.getActiveDoctors(); }
        catch (Exception e) { return java.util.Collections.emptyList(); }
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
