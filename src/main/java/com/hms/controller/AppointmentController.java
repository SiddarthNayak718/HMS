package com.hms.controller;

import com.hms.dao.AppointmentDAO;
import com.hms.dao.DoctorDAO;
import com.hms.dao.PatientDAO;
import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AppointmentController implements Initializable {

    // Table
    @FXML private TableView<Appointment>            apptTable;
    @FXML private TableColumn<Appointment, Integer> colId;
    @FXML private TableColumn<Appointment, String>  colPatient;
    @FXML private TableColumn<Appointment, String>  colDoctor;
    @FXML private TableColumn<Appointment, LocalDate> colDate;
    @FXML private TableColumn<Appointment, String>  colTime;
    @FXML private TableColumn<Appointment, String>  colReason;
    @FXML private TableColumn<Appointment, String>  colAStatus;

    // Form
    @FXML private ComboBox<Patient> cbPatient;
    @FXML private ComboBox<Doctor>  cbDoctor;
    @FXML private DatePicker        dpDate;
    @FXML private ComboBox<String>  cbTime;
    @FXML private TextArea          taReason;

    private final AppointmentDAO apptDAO    = new AppointmentDAO();
    private final PatientDAO     patientDAO = new PatientDAO();
    private final DoctorDAO      doctorDAO  = new DoctorDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("apptId"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("apptDate"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("apptTime"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colAStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        apptTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        apptTable.setPlaceholder(new Label("No appointments found."));

        // Load combos
        try { cbPatient.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients())); }
        catch (Exception e) { /* ignore */ }
        try { cbDoctor.setItems(FXCollections.observableArrayList(doctorDAO.getActiveDoctors())); }
        catch (Exception e) { /* ignore */ }

        cbTime.setItems(FXCollections.observableArrayList(
            "09:00","09:30","10:00","10:30","11:00","11:30",
            "12:00","14:00","14:30","15:00","15:30","16:00","16:30","17:00"));

        dpDate.setValue(LocalDate.now());

        refreshTable();
    }

    @FXML private void onShowAll() {
        try { apptTable.setItems(FXCollections.observableArrayList(apptDAO.getAllAppointments())); }
        catch (Exception ex) { AlertUtil.showError("Error", ex.getMessage()); }
    }

    @FXML private void onShowToday() {
        try { apptTable.setItems(FXCollections.observableArrayList(apptDAO.getTodaysAppointments())); }
        catch (Exception ex) { AlertUtil.showError("Error", ex.getMessage()); }
    }

    @FXML private void onRefresh() { refreshTable(); }

    @FXML private void onBook() {
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
            onClear();
            refreshTable();
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("UQ_APPT"))
                AlertUtil.showError("Conflict", "Doctor already has an appointment at that time.");
            else AlertUtil.showError("DB Error", ex.getMessage());
        }
    }

    @FXML private void onCancel()   { updateStatus("CANCELLED"); }
    @FXML private void onComplete() { updateStatus("COMPLETED"); }

    @FXML private void onClear() {
        cbPatient.setValue(null); cbDoctor.setValue(null);
        dpDate.setValue(LocalDate.now()); cbTime.setValue(null);
        taReason.clear();
    }

    private void updateStatus(String status) {
        Appointment sel = apptTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.showError("Select", "Please select an appointment."); return; }
        if (AlertUtil.showConfirm("Confirm", "Mark as " + status + "?")) {
            apptDAO.updateStatus(sel.getApptId(), status);
            refreshTable();
        }
    }

    private void refreshTable() {
        try { apptTable.setItems(FXCollections.observableArrayList(apptDAO.getAllAppointments())); }
        catch (Exception e) { /* DB not available */ }
    }
}
