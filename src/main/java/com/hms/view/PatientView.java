package com.hms.view;

import com.hms.dao.PatientDAO;
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

public class PatientView {

    private final PatientDAO dao = new PatientDAO();
    private TableView<Patient> table;

    // Form fields
    private TextField  tfFirst, tfLast, tfPhone, tfEmail, tfAddress, tfEmergency, tfBlood;
    private DatePicker dpDOB;
    private ComboBox<String> cbGender;
    private int editingId = -1;

    public Node getView() {
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.62);

        split.getItems().add(buildTablePane());
        split.getItems().add(buildFormPane());

        refreshTable(dao.getAllPatients());
        return split;
    }

    // -------------------------------------------------------
    // TABLE PANEL
    // -------------------------------------------------------
    private VBox buildTablePane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(16));

        Label title = new Label("Patient Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        // Search bar
        TextField search = new TextField();
        search.setPromptText("🔍  Search by name or phone…");
        search.setPrefWidth(300);
        search.setOnKeyReleased(e -> {
            String kw = search.getText().trim();
            if (kw.isEmpty()) refreshTable(dao.getAllPatients());
            else              refreshTable(dao.searchPatients(kw));
        });

        Button btnRefresh = new Button("↻ Refresh");
        btnRefresh.setOnAction(e -> {
            search.clear();
            refreshTable(dao.getAllPatients());
        });

        HBox top = new HBox(8, search, btnRefresh);
        top.setAlignment(Pos.CENTER_LEFT);

        // Table
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.getColumns().addAll(
            col("ID",          "patientId",      60),
            col("First Name",  "firstName",     120),
            col("Last Name",   "lastName",      120),
            col("DOB",         "dateOfBirth",   100),
            col("Gender",      "gender",         70),
            col("Blood",       "bloodGroup",     60),
            col("Phone",       "phone",         110),
            col("Status",      "status",         70)
        );

        // Row selection → fill form
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) populateForm(sel);
        });

        table.setPlaceholder(new Label("No patients found."));

        // Delete button
        Button btnDel = new Button("🗑 Deactivate Selected");
        btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6;");
        btnDel.setOnAction(e -> deleteSelected());

        pane.getChildren().addAll(title, top, table, btnDel);
        return pane;
    }

    // -------------------------------------------------------
    // FORM PANEL
    // -------------------------------------------------------
    private VBox buildFormPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(16));

        Label title = new Label("Patient Details");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a3c5e;");

        tfFirst     = field("First Name *");
        tfLast      = field("Last Name *");
        tfPhone     = field("Phone *");
        tfEmail     = field("Email");
        tfBlood     = field("Blood Group");
        tfAddress   = new TextField(); tfAddress.setPromptText("Address");
        tfEmergency = field("Emergency Contact");

        dpDOB = new DatePicker(LocalDate.of(1990, 1, 1));
        dpDOB.setPromptText("Date of Birth *");

        cbGender = new ComboBox<>(FXCollections.observableArrayList("MALE","FEMALE","OTHER"));
        cbGender.setPromptText("Gender *");
        cbGender.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.addRow(0, label("First Name"), tfFirst);
        grid.addRow(1, label("Last Name"),  tfLast);
        grid.addRow(2, label("Date of Birth"), dpDOB);
        grid.addRow(3, label("Gender"),     cbGender);
        grid.addRow(4, label("Blood Group"),tfBlood);
        grid.addRow(5, label("Phone"),      tfPhone);
        grid.addRow(6, label("Email"),      tfEmail);
        grid.addRow(7, label("Address"),    tfAddress);
        grid.addRow(8, label("Emergency"),  tfEmergency);
        ColumnConstraints c1 = new ColumnConstraints(110);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        Button btnSave = new Button("💾 Save");
        btnSave.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                         "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18;");
        btnSave.setOnAction(e -> savePatient());

        Button btnClear = new Button("✖ Clear");
        btnClear.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                          "-fx-background-radius: 6; -fx-padding: 8 14;");
        btnClear.setOnAction(e -> clearForm());

        HBox buttons = new HBox(10, btnSave, btnClear);
        pane.getChildren().addAll(title, grid, buttons);
        return pane;
    }

    // -------------------------------------------------------
    // ACTIONS
    // -------------------------------------------------------
    private void savePatient() {
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
            clearForm();
            refreshTable(dao.getAllPatients());
        } catch (Exception ex) {
            AlertUtil.showError("DB Error", ex.getMessage());
        }
    }

    private void deleteSelected() {
        Patient sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtil.showError("Select", "Please select a patient."); return; }
        if (AlertUtil.showConfirm("Deactivate", "Deactivate patient: " + sel.getFullName() + "?")) {
            dao.deletePatient(sel.getPatientId());
            clearForm();
            refreshTable(dao.getAllPatients());
        }
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

    private void clearForm() {
        editingId = -1;
        tfFirst.clear(); tfLast.clear(); tfPhone.clear(); tfEmail.clear();
        tfBlood.clear(); tfAddress.clear(); tfEmergency.clear();
        dpDOB.setValue(LocalDate.of(1990,1,1));
        cbGender.setValue(null);
        table.getSelectionModel().clearSelection();
    }

    private void refreshTable(List<Patient> list) {
        table.setItems(FXCollections.observableArrayList(list));
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------
    private TextField field(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); return tf;
    }
    private Label label(String text) {
        Label l = new Label(text); l.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;"); return l;
    }
    @SuppressWarnings("unchecked")
    private TableColumn col(String h, String prop, int min) {
        TableColumn tc = new TableColumn(h);
        tc.setCellValueFactory(new PropertyValueFactory<>(prop));
        tc.setMinWidth(min);
        return tc;
    }
}
