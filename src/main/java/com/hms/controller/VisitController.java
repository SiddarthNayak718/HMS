package com.hms.controller;

import com.hms.dao.PatientDAO;
import com.hms.dao.DoctorDAO;
import com.hms.dao.VisitDAO;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.Visit;
import com.hms.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class VisitController implements Initializable {
    @FXML private TableView<Visit> visitTable;
    @FXML private TableColumn<Visit, String> idCol, patientCol, doctorCol, dateCol, diagnosisCol;
    
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextArea diagnosisArea, notesArea;

    private VisitDAO visitDAO = new VisitDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private DoctorDAO doctorDAO = new DoctorDAO();
    private ObservableList<Visit> visitList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadCombos();
        loadData();
    }

    private void setupTable() {
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getVisitId())));
        patientCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPatientId())));
        doctorCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getDoctorId())));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVisitDate().toString()));
        diagnosisCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDiagnosis()));
    }

    private void loadCombos() {
        try {
            patientCombo.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients()));
            doctorCombo.setItems(FXCollections.observableArrayList(doctorDAO.getAllDoctors()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            visitList = FXCollections.observableArrayList(visitDAO.getAllVisits());
            visitTable.setItems(visitList);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "Failed to load visits.");
        }
    }

    @FXML private void onSave() {
        if (patientCombo.getValue() == null || doctorCombo.getValue() == null || datePicker.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please fill required fields (Patient, Doctor, Date).");
            return;
        }

        Visit v = new Visit();
        v.setPatientId(patientCombo.getValue().getPatientId());
        v.setDoctorId(doctorCombo.getValue().getDoctorId());
        v.setVisitDate(Date.valueOf(datePicker.getValue()));
        v.setDiagnosis(diagnosisArea.getText().trim());
        v.setNotes(notesArea.getText().trim());
        
        try {
            visitDAO.addVisit(v);
            AlertUtil.showSuccess("Success", "Visit logged successfully.");
            loadData();
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not save visit.");
        }
    }

    @FXML private void onClear() {
        clearForm();
    }

    private void clearForm() {
        patientCombo.setValue(null);
        doctorCombo.setValue(null);
        datePicker.setValue(null);
        diagnosisArea.clear();
        notesArea.clear();
    }
}
