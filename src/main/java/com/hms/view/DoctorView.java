package com.hms.view;

import com.hms.dao.DoctorDAO;
import com.hms.dao.DepartmentDAO;
import com.hms.model.Doctor;
import com.hms.model.Department;
import com.hms.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class DoctorView {

    private final DoctorDAO     doctorDAO = new DoctorDAO();
    private final DepartmentDAO deptDAO   = new DepartmentDAO();

    private TableView<Doctor> table;

    private TextField    tfFirst, tfLast, tfSpec, tfPhone, tfEmail, tfQual;
    private ComboBox<Department> cbDept;
    private ComboBox<String>     cbStatus;
    private int editingId = -1;

    public Node getView() {
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.62);
        split.getItems().addAll(buildTablePane(), buildFormPane());
        refreshTable(doctorDAO.getAllDoctors());
        return split;
    }

    // -------------------------------------------------------
    private VBox buildTablePane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(16));

        Label title = new Label("Doctor Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.setOnAction(e -> refreshTable(doctorDAO.getAllDoctors()));

        HBox top = new HBox(8, btnRefresh);
        top.setAlignment(Pos.CENTER_LEFT);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.getColumns().addAll(
            col("ID",            "doctorId",      60),
            col("First Name",    "firstName",    120),
            col("Last Name",     "lastName",     120),
            col("Specialization","specialization",160),
            col("Department",    "deptName",     140),
            col("Phone",         "phone",        110),
            col("Status",        "status",        70)
        );

        table.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, sel) -> { if (sel != null) populateForm(sel); });
        table.setPlaceholder(new Label("No doctors found."));

        pane.getChildren().addAll(title, top, table);
        return pane;
    }

    // -------------------------------------------------------
    private VBox buildFormPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(16));

        Label title = new Label("Doctor Details");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        tfFirst = field("First Name *");
        tfLast  = field("Last Name *");
        tfSpec  = field("Specialization");
        tfPhone = field("Phone *");
        tfEmail = field("Email");
        tfQual  = field("Qualification");

        List<Department> depts = deptDAO.getAllDepartments();
        cbDept = new ComboBox<>(FXCollections.observableArrayList(depts));
        cbDept.setPromptText("Select Department");
        cbDept.setMaxWidth(Double.MAX_VALUE);

        cbStatus = new ComboBox<>(FXCollections.observableArrayList("ACTIVE","INACTIVE"));
        cbStatus.setValue("ACTIVE");
        cbStatus.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.addRow(0, lbl("First Name"),     tfFirst);
        grid.addRow(1, lbl("Last Name"),      tfLast);
        grid.addRow(2, lbl("Specialization"), tfSpec);
        grid.addRow(3, lbl("Department"),     cbDept);
        grid.addRow(4, lbl("Phone"),          tfPhone);
        grid.addRow(5, lbl("Email"),          tfEmail);
        grid.addRow(6, lbl("Qualification"),  tfQual);
        grid.addRow(7, lbl("Status"),         cbStatus);

        ColumnConstraints c1 = new ColumnConstraints(110);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        Button btnSave = new Button("💾 Save");
        btnSave.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                         "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18;");
        btnSave.setOnAction(e -> saveDoctor());

        Button btnClear = new Button("✖ Clear");
        btnClear.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                          "-fx-background-radius: 6; -fx-padding: 8 14;");
        btnClear.setOnAction(e -> clearForm());

        HBox buttons = new HBox(10, btnSave, btnClear);
        pane.getChildren().addAll(title, grid, buttons);
        return pane;
    }

    // -------------------------------------------------------
    private void saveDoctor() {
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
            clearForm();
            refreshTable(doctorDAO.getAllDoctors());
        } catch (Exception ex) {
            AlertUtil.showError("DB Error", ex.getMessage());
        }
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
        // Select matching dept
        cbDept.getItems().stream()
            .filter(dep -> dep.getDeptId() == d.getDeptId())
            .findFirst().ifPresent(cbDept::setValue);
    }

    private void clearForm() {
        editingId = -1;
        tfFirst.clear(); tfLast.clear(); tfSpec.clear();
        tfPhone.clear(); tfEmail.clear(); tfQual.clear();
        cbDept.setValue(null); cbStatus.setValue("ACTIVE");
        table.getSelectionModel().clearSelection();
    }

    private void refreshTable(List<Doctor> list) {
        table.setItems(FXCollections.observableArrayList(list));
    }

    private TextField field(String p) { TextField tf = new TextField(); tf.setPromptText(p); return tf; }
    private Label     lbl(String t)   { Label l = new Label(t); l.setStyle("-fx-font-weight:bold;"); return l; }

    @SuppressWarnings("unchecked")
    private TableColumn col(String h, String prop, int min) {
        TableColumn tc = new TableColumn(h);
        tc.setCellValueFactory(new PropertyValueFactory<>(prop));
        tc.setMinWidth(min);
        return tc;
    }
}
