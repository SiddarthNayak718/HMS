package com.hms.model;

import java.sql.Date;

public class Prescription {
    private int prescId;
    private int visitId;
    private int patientId;
    private int doctorId;
    private Date prescDate;
    private String notes;

    public Prescription() {}

    public Prescription(int prescId, int visitId, int patientId, int doctorId, Date prescDate, String notes) {
        this.prescId = prescId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.prescDate = prescDate;
        this.notes = notes;
    }

    public int getPrescId() { return prescId; }
    public void setPrescId(int prescId) { this.prescId = prescId; }

    public int getVisitId() { return visitId; }
    public void setVisitId(int visitId) { this.visitId = visitId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public Date getPrescDate() { return prescDate; }
    public void setPrescDate(Date prescDate) { this.prescDate = prescDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
