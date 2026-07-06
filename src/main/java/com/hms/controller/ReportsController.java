package com.hms.controller;

import com.hms.dao.AppointmentDAO;
import com.hms.dao.BillDAO;
import com.hms.dao.MedicineDAO;
import com.hms.dao.PatientDAO;
import com.hms.model.Appointment;
import com.hms.model.Bill;
import com.hms.model.Medicine;
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
import java.util.stream.Collectors;

public class ReportsController implements Initializable {

    // Tab 1 – Daily Appointments
    @FXML private DatePicker                         dpApptDate;
    @FXML private TableView<Appointment>             apptReportTable;
    @FXML private TableColumn<Appointment, Integer>  colRApptId;
    @FXML private TableColumn<Appointment, String>   colRPatient;
    @FXML private TableColumn<Appointment, String>   colRDoctor;
    @FXML private TableColumn<Appointment, LocalDate> colRDate;
    @FXML private TableColumn<Appointment, String>   colRTime;
    @FXML private TableColumn<Appointment, String>   colRReason;
    @FXML private TableColumn<Appointment, String>   colRStatus;

    // Tab 2 – Patient History
    @FXML private ComboBox<Patient>                  cbHistoryPatient;
    @FXML private TableView<Appointment>             historyTable;
    @FXML private TableColumn<Appointment, Integer>  colHApptId;
    @FXML private TableColumn<Appointment, String>   colHPatient;
    @FXML private TableColumn<Appointment, String>   colHDoctor;
    @FXML private TableColumn<Appointment, LocalDate> colHDate;
    @FXML private TableColumn<Appointment, String>   colHTime;
    @FXML private TableColumn<Appointment, String>   colHReason;
    @FXML private TableColumn<Appointment, String>   colHStatus;

    // Tab 3 – Revenue
    @FXML private Label                              lblTodayRevenue;
    @FXML private TableView<Bill>                    revenueTable;
    @FXML private TableColumn<Bill, Integer>         colRevBillId;
    @FXML private TableColumn<Bill, String>          colRevPatient;
    @FXML private TableColumn<Bill, ?>               colRevDate;
    @FXML private TableColumn<Bill, Double>          colRevTotal;
    @FXML private TableColumn<Bill, Double>          colRevDisc;
    @FXML private TableColumn<Bill, Double>          colRevNet;
    @FXML private TableColumn<Bill, String>          colRevStatus;

    // Tab 4 – Low Stock
    @FXML private TableView<Medicine>                lowStockTable;
    @FXML private TableColumn<Medicine, Integer>     colLsId;
    @FXML private TableColumn<Medicine, String>      colLsName;
    @FXML private TableColumn<Medicine, String>      colLsCat;
    @FXML private TableColumn<Medicine, Integer>     colLsStock;
    @FXML private TableColumn<Medicine, String>      colLsMfr;

    private final AppointmentDAO apptDAO    = new AppointmentDAO();
    private final PatientDAO     patientDAO = new PatientDAO();
    private final BillDAO        billDAO    = new BillDAO();
    private final MedicineDAO    medDAO     = new MedicineDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Tab 1 columns
        colRApptId.setCellValueFactory(new PropertyValueFactory<>("apptId"));
        colRPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colRDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colRDate.setCellValueFactory(new PropertyValueFactory<>("apptDate"));
        colRTime.setCellValueFactory(new PropertyValueFactory<>("apptTime"));
        colRReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colRStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Tab 2 columns
        colHApptId.setCellValueFactory(new PropertyValueFactory<>("apptId"));
        colHPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colHDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colHDate.setCellValueFactory(new PropertyValueFactory<>("apptDate"));
        colHTime.setCellValueFactory(new PropertyValueFactory<>("apptTime"));
        colHReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colHStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Tab 3 columns
        colRevBillId.setCellValueFactory(new PropertyValueFactory<>("billId"));
        colRevPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colRevDate.setCellValueFactory(new PropertyValueFactory<>("billDate"));
        colRevTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colRevDisc.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colRevNet.setCellValueFactory(new PropertyValueFactory<>("netAmount"));
        colRevStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Tab 4 columns
        colLsId.setCellValueFactory(new PropertyValueFactory<>("medicineId"));
        colLsName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLsCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        colLsStock.setCellValueFactory(new PropertyValueFactory<>("stockQty"));
        colLsMfr.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));

        lowStockTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        lowStockTable.setPlaceholder(new Label("✅ All medicines have adequate stock."));

        apptReportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        revenueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        dpApptDate.setValue(LocalDate.now());

        // Load patient combo
        try { cbHistoryPatient.setItems(FXCollections.observableArrayList(patientDAO.getAllPatients())); }
        catch (Exception e) { /* ignore */ }

        // Auto-load each tab
        loadTodayAppointments();
        loadRevenue();
        loadLowStock();
    }

    @FXML private void onLoadAppointments() {
        try {
            LocalDate sel = dpApptDate.getValue();
            List<Appointment> all = apptDAO.getAllAppointments();
            apptReportTable.setItems(FXCollections.observableArrayList(
                all.stream().filter(a -> sel.equals(a.getApptDate()))
                   .collect(Collectors.toList())));
        } catch (Exception ex) { AlertUtil.showError("Error", ex.getMessage()); }
    }

    @FXML private void onLoadHistory() {
        if (cbHistoryPatient.getValue() == null) return;
        try {
            historyTable.setItems(FXCollections.observableArrayList(
                apptDAO.getByPatient(cbHistoryPatient.getValue().getPatientId())));
        } catch (Exception ex) { AlertUtil.showError("Error", ex.getMessage()); }
    }

    private void loadTodayAppointments() {
        try { apptReportTable.setItems(
                FXCollections.observableArrayList(apptDAO.getTodaysAppointments())); }
        catch (Exception e) { /* ignore */ }
    }

    private void loadRevenue() {
        try {
            double rev = billDAO.getTodaysRevenue();
            lblTodayRevenue.setText("Today's Revenue: ₹" + String.format("%.2f", rev));
            revenueTable.setItems(FXCollections.observableArrayList(billDAO.getAllBills()));
        } catch (Exception e) {
            lblTodayRevenue.setText("Today's Revenue: N/A");
        }
    }

    private void loadLowStock() {
        try { lowStockTable.setItems(FXCollections.observableArrayList(medDAO.getLowStock(50))); }
        catch (Exception e) { /* ignore */ }
    }
}
