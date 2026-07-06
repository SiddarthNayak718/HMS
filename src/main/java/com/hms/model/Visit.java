package com.hms.model;

import java.sql.Date;

public class Visit {
    private int visitId;
    private Integer apptId;
    private int patientId;
    private int doctorId;
    private Date visitDate;
    private String diagnosis;
    private String notes;
    private Date followUpDate;

    public Visit(int visitId, Integer apptId, int patientId, int doctorId, Date visitDate, String diagnosis, String notes, Date followUpDate) {
        this.visitId = visitId;
        this.apptId = apptId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.visitDate = visitDate;
        this.diagnosis = diagnosis;
        this.notes = notes;
        this.followUpDate = followUpDate;
    }

    public Visit() {}

    public int getVisitId() { return visitId; }
    public void setVisitId(int visitId) { this.visitId = visitId; }

    public Integer getApptId() { return apptId; }
    public void setApptId(Integer apptId) { this.apptId = apptId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public Date getVisitDate() { return visitDate; }
    public void setVisitDate(Date visitDate) { this.visitDate = visitDate; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Date getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(Date followUpDate) { this.followUpDate = followUpDate; }
}
