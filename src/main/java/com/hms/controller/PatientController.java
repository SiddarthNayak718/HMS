package com.hms.controller;

import com.hms.dao.PatientDAO;
import com.hms.model.Patient;
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

public class PatientController implements Initializable {

    // Table
    @FXML private TableView<Patient>               patientTable;
    @FXML private TableColumn<Patient, Integer>    colId;
    @FXML private TableColumn<Patient, String>     colFirstName;
    @FXML private TableColumn<Patient, String>     colLastName;
    @FXML private TableColumn<Patient, LocalDate>  colDOB;
    @FXML private TableColumn<Patient, String>     colGender;
    @FXML private TableColumn<Patient, String>     colBlood;
    @FXML private TableColumn<Patient, String>     colPhone;
    @FXML private TableColumn<Patient, String>     colPStatus;

    // Form
    @FXML private TextField        tfFirst;
    @FXML private TextField        tfLast;
    @FXML private DatePicker       dpDOB;
    @FXML private ComboBox<String> cbGender;
    @FXML private TextField        tfBlood;
    @FXML private TextField        tfPhone;
    @FXML private TextField        tfEmail;
    @FXML private TextField        tfAddress;
    @FXML private TextField        tfEmergency;

    // Search
    @FXML private TextField searchField;

    private final PatientDAO dao = new PatientDAO();
    private int editingId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colDOB.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colBlood.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        patientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        patientTable.setPlaceholder(new Label("No patients found."));

        cbGender.setItems(FXCollections.observableArrayList("MALE", "FEMALE", "OTHER"));
        dpDOB.setValue(LocalDate.of(1990, 1, 1));

        patientTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, sel) -> { if (sel != null) populateForm(sel); });

        searchField.setOnKeyReleased(e -> {
            String kw = searchField.getText().trim();
            refreshTable(kw.isEmpty() ? dao.getAllPatients() : dao.searchPatients(kw));
        });

        refreshTable(dao.getAllPatients());
    }

    @FXML private void onRefresh() {
        searchField.clear();
        refreshTable(dao.getAllPatients());
    }

    @FXML private void onSave() {
        if (tfFirst.getText().isBlank() || tfLast.getText().isBlank() ||
            tfPhone.getText().isBlank() || dpDOB.getValue() == null ||
            cbGender.getValue() == null) {
            AlertUtil.showError("Validation", "Fields marked * are required.");
            return;
        }
        Patient p = new Patient();
        p.setFirstName(tfFirst.getText().trim());
        p.setLastName(tfLast.getText().trim());
        p.setDateOfBirth(dpDOB.getValue());
        p.setGender(cbGender.getValue());
        p.setBloodGroup(tfBlood.getText().trim());
        p.setPhone(tfPhone.getText().trim());
        p.setEmail(tfEmail.getText().trim());
        p.setAddress(tfAddress.getText().trim());
        p.setEmergencyContact(tfEmergency.getText().trim());

        try {
            if (editingId == -1) {
                dao.insertPatient(p);
                AlertUtil.showInfo("Success", "Patient registered successfully.");
            } else {
                p.setPatientId(editingId);
                p.setStatus("ACTIVE");
                dao.updatePatient(p);
                AlertUtil.showInfo("Success", "Patient updated successfully.");
            }
            onClear();
            refreshTable(dao.getAllPatients());
        } catch (Exception ex) {
            AlertUtil.showError("DB Error", ex.getMessage());
        }
    }

    @FXML private void onDeactivate() {
        Patient sel = patientTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.showError("Select", "Please select a patient."); return; }
        if (AlertUtil.showConfirm("Deactivate", "Deactivate patient: " + sel.getFullName() + "?")) {
            dao.deletePatient(sel.getPatientId());
            onClear();
            refreshTable(dao.getAllPatients());
        }
    }

    @FXML private void onClear() {
        editingId = -1;
        tfFirst.clear(); tfLast.clear(); tfPhone.clear(); tfEmail.clear();
        tfBlood.clear(); tfAddress.clear(); tfEmergency.clear();
        dpDOB.setValue(LocalDate.of(1990, 1, 1));
        cbGender.setValue(null);
        patientTable.getSelectionModel().clearSelection();
    }

    private void populateForm(Patient p) {
        editingId = p.getPatientId();
        tfFirst.setText(p.getFirstName());
        tfLast.setText(p.getLastName());
        dpDOB.setValue(p.getDateOfBirth());
        cbGender.setValue(p.getGender());
        tfBlood.setText(p.getBloodGroup() != null ? p.getBloodGroup() : "");
        tfPhone.setText(p.getPhone());
        tfEmail.setText(p.getEmail() != null ? p.getEmail() : "");
        tfAddress.setText(p.getAddress() != null ? p.getAddress() : "");
        tfEmergency.setText(p.getEmergencyContact() != null ? p.getEmergencyContact() : "");
    }

    private void refreshTable(List<Patient> list) {
        patientTable.setItems(FXCollections.observableArrayList(list));
    }
}
