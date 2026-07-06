package com.hms.model;

import java.time.LocalDate;

public class Bill {
    private int    billId;
    private int    patientId;
    private String patientName;
    private int    visitId;
    private LocalDate billDate;
    private double consultationFee;
    private double medicineAmount;
    private double labAmount;
    private double totalAmount;
    private double discount;
    private double netAmount;
    private String status;

    public Bill() {}
    public Bill(int billId, int patientId, String patientName, int visitId,
                LocalDate billDate, double consultationFee, double medicineAmount,
                double labAmount, double totalAmount, double discount,
                double netAmount, String status) {
        this.billId = billId; this.patientId = patientId; this.patientName = patientName;
        this.visitId = visitId; this.billDate = billDate; this.consultationFee = consultationFee;
        this.medicineAmount = medicineAmount; this.labAmount = labAmount;
        this.totalAmount = totalAmount; this.discount = discount;
        this.netAmount = netAmount; this.status = status;
    }

    public int    getBillId()               { return billId; }
    public void   setBillId(int v)          { billId = v; }
    public int    getPatientId()            { return patientId; }
    public void   setPatientId(int v)       { patientId = v; }
    public String getPatientName()          { return patientName; }
    public void   setPatientName(String v)  { patientName = v; }
    public int    getVisitId()              { return visitId; }
    public void   setVisitId(int v)         { visitId = v; }
    public LocalDate getBillDate()          { return billDate; }
    public void   setBillDate(LocalDate v)  { billDate = v; }
    public double getConsultationFee()      { return consultationFee; }
    public void   setConsultationFee(double v) { consultationFee = v; }
    public double getMedicineAmount()       { return medicineAmount; }
    public void   setMedicineAmount(double v) { medicineAmount = v; }
    public double getLabAmount()            { return labAmount; }
    public void   setLabAmount(double v)    { labAmount = v; }
    public double getTotalAmount()          { return totalAmount; }
    public void   setTotalAmount(double v)  { totalAmount = v; }
    public double getDiscount()             { return discount; }
    public void   setDiscount(double v)     { discount = v; }
    public double getNetAmount()            { return netAmount; }
    public void   setNetAmount(double v)    { netAmount = v; }
    public String getStatus()               { return status; }
    public void   setStatus(String v)       { status = v; }
}
