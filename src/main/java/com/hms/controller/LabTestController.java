package com.hms.controller;

import com.hms.dao.LabTestDAO;
import com.hms.dao.TestOrderDAO;
import com.hms.dao.VisitDAO;
import com.hms.model.LabTest;
import com.hms.model.TestOrder;
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

public class LabTestController implements Initializable {

    @FXML private TableView<TestOrder> orderTable;
    @FXML private TableColumn<TestOrder, String> idCol, patientCol, testCol, dateCol, statusCol, resultCol;

    @FXML private ComboBox<Visit> visitCombo;
    @FXML private ComboBox<LabTest> testCombo;
    @FXML private DatePicker orderDatePicker;
    @FXML private TextField resultField;
    @FXML private Button updateResultBtn;

    private TestOrderDAO testOrderDAO = new TestOrderDAO();
    private LabTestDAO labTestDAO = new LabTestDAO();
    private VisitDAO visitDAO = new VisitDAO();

    private ObservableList<TestOrder> orderList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadCombos();
        loadData();
        
        visitCombo.setCellFactory(param -> new ListCell<Visit>() {
            @Override
            protected void updateItem(Visit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Visit " + item.getVisitId() + " - Pt: " + item.getPatientId());
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
                    setText("Visit " + item.getVisitId() + " - Pt: " + item.getPatientId());
                }
            }
        });
        
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && "PENDING".equals(newSel.getStatus())) {
                updateResultBtn.setDisable(false);
            } else {
                updateResultBtn.setDisable(true);
            }
        });
        
        updateResultBtn.setDisable(true);
    }

    private void setupTable() {
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getOrderId())));
        patientCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPatientName()));
        testCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTestName()));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderDate().toString()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        resultCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getResultValue() != null ? data.getValue().getResultValue() : "-"
        ));
    }

    private void loadCombos() {
        try {
            visitCombo.setItems(FXCollections.observableArrayList(visitDAO.getAllVisits()));
            testCombo.setItems(FXCollections.observableArrayList(labTestDAO.getAllLabTests()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            orderList = FXCollections.observableArrayList(testOrderDAO.getAllTestOrders());
            orderTable.setItems(orderList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void onOrderTest() {
        Visit v = visitCombo.getValue();
        LabTest t = testCombo.getValue();
        
        if (v == null || t == null) {
            AlertUtil.showError("Validation", "Please select a Visit and a Lab Test.");
            return;
        }

        TestOrder to = new TestOrder();
        to.setVisitId(v.getVisitId());
        to.setPatientId(v.getPatientId());
        to.setTestId(t.getTestId());
        to.setOrderedBy(v.getDoctorId());
        
        LocalDate ldate = orderDatePicker.getValue() != null ? orderDatePicker.getValue() : LocalDate.now();
        to.setOrderDate(Date.valueOf(ldate));
        
        try {
            testOrderDAO.addTestOrder(to);
            AlertUtil.showSuccess("Success", "Test Ordered Successfully.");
            loadData();
            visitCombo.setValue(null);
            testCombo.setValue(null);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not order test.");
        }
    }

    @FXML private void onUpdateResult() {
        TestOrder sel = orderTable.getSelectionModel().getSelectedItem();
        if (sel == null || resultField.getText().trim().isEmpty()) {
            AlertUtil.showError("Validation", "Select a pending order and enter the result.");
            return;
        }
        
        try {
            testOrderDAO.updateResult(sel.getOrderId(), resultField.getText().trim());
            AlertUtil.showSuccess("Success", "Results updated!");
            resultField.clear();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to update result.");
        }
    }
}
