package com.hms.controller;

import com.hms.dao.DepartmentDAO;
import com.hms.dao.DoctorDAO;
import com.hms.model.Department;
import com.hms.model.Doctor;
import com.hms.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DoctorController implements Initializable {

    // Table
    @FXML private TableView<Doctor>            doctorTable;
    @FXML private TableColumn<Doctor, Integer> colId;
    @FXML private TableColumn<Doctor, String>  colFirst;
    @FXML private TableColumn<Doctor, String>  colLast;
    @FXML private TableColumn<Doctor, String>  colSpec;
    @FXML private TableColumn<Doctor, String>  colDept;
    @FXML private TableColumn<Doctor, String>  colPhone;
    @FXML private TableColumn<Doctor, String>  colDStatus;

    // Form
    @FXML private TextField              tfFirst;
    @FXML private TextField              tfLast;
    @FXML private TextField              tfSpec;
    @FXML private ComboBox<Department>   cbDept;
    @FXML private TextField              tfPhone;
    @FXML private TextField              tfEmail;
    @FXML private TextField              tfQual;
    @FXML private ComboBox<String>       cbStatus;

    private final DoctorDAO     doctorDAO = new DoctorDAO();
    private final DepartmentDAO deptDAO   = new DepartmentDAO();
    private int editingId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        colFirst.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLast.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colDept.setCellValueFactory(new PropertyValueFactory<>("deptName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colDStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        doctorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        doctorTable.setPlaceholder(new Label("No doctors found."));

        cbStatus.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        cbStatus.setValue("ACTIVE");

        try {
            cbDept.setItems(FXCollections.observableArrayList(deptDAO.getAllDepartments()));
        } catch (Exception e) { /* ignore */ }

        doctorTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, sel) -> { if (sel != null) populateForm(sel); });

        refreshTable(doctorDAO.getAllDoctors());
    }

    @FXML private void onRefresh() {
        refreshTable(doctorDAO.getAllDoctors());
    }

    @FXML private void onSave() {
        if (tfFirst.getText().isBlank() || tfLast.getText().isBlank() ||
            tfPhone.getText().isBlank()) {
            AlertUtil.showError("Validation", "First name, last name, and phone are required.");
            return;
        }
        Doctor d = new Doctor();
        d.setFirstName(tfFirst.getText().trim());
        d.setLastName(tfLast.getText().trim());
        d.setSpecialization(tfSpec.getText().trim());
        d.setPhone(tfPhone.getText().trim());
        d.setEmail(tfEmail.getText().trim());
        d.setQualification(tfQual.getText().trim());
        d.setStatus(cbStatus.getValue() != null ? cbStatus.getValue() : "ACTIVE");
        if (cbDept.getValue() != null) d.setDeptId(cbDept.getValue().getDeptId());

        try {
            if (editingId == -1) {
                doctorDAO.insertDoctor(d);
                AlertUtil.showInfo("Success", "Doctor added successfully.");
            } else {
                d.setDoctorId(editingId);
                doctorDAO.updateDoctor(d);
                AlertUtil.showInfo("Success", "Doctor updated successfully.");
            }
            onClear();
            refreshTable(doctorDAO.getAllDoctors());
        } catch (Exception ex) {
            AlertUtil.showError("DB Error", ex.getMessage());
        }
    }

    @FXML private void onClear() {
        editingId = -1;
        tfFirst.clear(); tfLast.clear(); tfSpec.clear();
        tfPhone.clear(); tfEmail.clear(); tfQual.clear();
        cbDept.setValue(null);
        cbStatus.setValue("ACTIVE");
        doctorTable.getSelectionModel().clearSelection();
    }

    private void populateForm(Doctor d) {
        editingId = d.getDoctorId();
        tfFirst.setText(d.getFirstName());
        tfLast.setText(d.getLastName());
        tfSpec.setText(d.getSpecialization() != null ? d.getSpecialization() : "");
        tfPhone.setText(d.getPhone() != null ? d.getPhone() : "");
        tfEmail.setText(d.getEmail() != null ? d.getEmail() : "");
        tfQual.setText(d.getQualification() != null ? d.getQualification() : "");
        cbStatus.setValue(d.getStatus());
        cbDept.getItems().stream()
            .filter(dep -> dep.getDeptId() == d.getDeptId())
            .findFirst().ifPresent(cbDept::setValue);
    }

    private void refreshTable(List<Doctor> list) {
        doctorTable.setItems(FXCollections.observableArrayList(list));
    }
}
