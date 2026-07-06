package com.hms.controller;

import com.hms.dao.*;
import com.hms.model.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PrescriptionController implements Initializable {

    @FXML private TableView<Prescription> prescTable;
    @FXML private TableColumn<Prescription, String> idCol, visitIdCol, dateCol;

    @FXML private ListView<PrescriptionItem> itemListView;
    
    @FXML private ComboBox<Visit> visitCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextArea notesArea;

    @FXML private ComboBox<Medicine> medicineCombo;
    @FXML private TextField dosageField, frequencyField, daysField, quantityField;

    private PrescriptionDAO prescDAO = new PrescriptionDAO();
    private VisitDAO visitDAO = new VisitDAO();
    private MedicineDAO medicineDAO = new MedicineDAO();
    
    private ObservableList<Prescription> prescList;
    private ObservableList<PrescriptionItem> currentItems = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadCombos();
        loadData();
        itemListView.setItems(currentItems);
        itemListView.setCellFactory(param -> new ListCell<PrescriptionItem>() {
            @Override
            protected void updateItem(PrescriptionItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMedicineName() + " - " + item.getDosage() + " (" + item.getQuantity() + " Qty)");
                }
            }
        });
        
        // Setup VisitCombo Custom Renderer
        visitCombo.setCellFactory(param -> new ListCell<Visit>() {
            @Override
            protected void updateItem(Visit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Visit " + item.getVisitId() + " (" + item.getVisitDate() + ")");
                }
            }
        });
        visitCombo.setButtonCell(new ListCell<Visit>() {
            @Override
            protected void updateItem(Visit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Visit " + item.getVisitId() + " (" + item.getVisitDate() + ")");
                }
            }
        });
    }

    private void setupTable() {
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPrescId())));
        visitIdCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getVisitId())));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrescDate().toString()));
    }

    private void loadCombos() {
        try {
            visitCombo.setItems(FXCollections.observableArrayList(visitDAO.getAllVisits()));
            medicineCombo.setItems(FXCollections.observableArrayList(medicineDAO.getAllMedicines()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            prescList = FXCollections.observableArrayList(prescDAO.getAllPrescriptions());
            prescTable.setItems(prescList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void onAddItem() {
        Medicine med = medicineCombo.getValue();
        if (med == null || quantityField.getText().isEmpty()) {
            AlertUtil.showError("Validation", "Select a medicine and specify quantity.");
            return;
        }
        
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            int days = daysField.getText().isEmpty() ? 0 : Integer.parseInt(daysField.getText().trim());
            
            PrescriptionItem pi = new PrescriptionItem();
            pi.setMedicineId(med.getMedicineId());
            pi.setMedicineName(med.getName()); 
            pi.setDosage(dosageField.getText());
            pi.setFrequency(frequencyField.getText());
            pi.setDurationDays(days > 0 ? days : null);
            pi.setQuantity(qty);
            
            currentItems.add(pi);
            
            // Clear item inputs
            medicineCombo.setValue(null);
            dosageField.clear();
            frequencyField.clear();
            daysField.clear();
            quantityField.clear();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation", "Please enter valid numbers for Quantity and Duration.");
        }
    }

    @FXML private void onSavePrescription() {
        Visit selectedVisit = visitCombo.getValue();
        if (selectedVisit == null || currentItems.isEmpty()) {
            AlertUtil.showError("Validation", "Please select a Visit and add at least one item.");
            return;
        }

        Prescription p = new Prescription();
        p.setVisitId(selectedVisit.getVisitId());
        p.setPatientId(selectedVisit.getPatientId());
        p.setDoctorId(selectedVisit.getDoctorId());
        LocalDate dp = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
        p.setPrescDate(Date.valueOf(dp));
        p.setNotes(notesArea.getText());

        try {
            prescDAO.addPrescriptionWithItems(p, new ArrayList<>(currentItems));
            AlertUtil.showSuccess("Success", "Prescription Saved. Medication Stock Updated!");
            
            // Refresh
            loadData();
            onClear();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not save prescription.");
        }
    }

    @FXML private void onClear() {
        visitCombo.setValue(null);
        datePicker.setValue(null);
        notesArea.clear();
        currentItems.clear();
        medicineCombo.setValue(null);
    }
}
