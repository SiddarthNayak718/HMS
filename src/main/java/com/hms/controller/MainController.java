package com.hms.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadView("Dashboard");
    }

    @FXML private void onDashboard()    { loadView("Dashboard"); }
    @FXML private void onPatients()     { loadView("Patient"); }
    @FXML private void onDoctors()      { loadView("Doctor"); }
    @FXML private void onAppointments() { loadView("Appointment"); }
    @FXML private void onVisits()       { loadView("Visit"); }
    @FXML private void onPrescriptions(){ loadView("Prescription"); }
    @FXML private void onLabTests()     { loadView("LabTest"); }
    @FXML private void onMedicines()    { loadView("Medicine"); }
    @FXML private void onBilling()      { loadView("Billing"); }
    @FXML private void onReports()      { loadView("Reports"); }

    private void loadView(String name) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/" + name + ".fxml");
            if (fxmlUrl == null) {
                System.err.println("FXML not found: /fxml/" + name + ".fxml");
                return;
            }
            Node view = FXMLLoader.load(fxmlUrl);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
