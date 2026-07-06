package com.hms.model;

import java.time.LocalDate;

// ============================================================
// Appointment
// ============================================================
public class Appointment {
    private int    apptId;
    private int    patientId;
    private String patientName;
    private int    doctorId;
    private String doctorName;
    private LocalDate apptDate;
    private String apptTime;
    private String reason;
    private String status;

    public Appointment() {}
    public Appointment(int apptId, int patientId, String patientName,
                       int doctorId, String doctorName,
                       LocalDate apptDate, String apptTime,
                       String reason, String status) {
        this.apptId = apptId; this.patientId = patientId;
        this.patientName = patientName; this.doctorId = doctorId;
        this.doctorName = doctorName; this.apptDate = apptDate;
        this.apptTime = apptTime; this.reason = reason; this.status = status;
    }

    public int       getApptId()          { return apptId; }
    public void      setApptId(int v)     { apptId = v; }
    public int       getPatientId()       { return patientId; }
    public void      setPatientId(int v)  { patientId = v; }
    public String    getPatientName()     { return patientName; }
    public void      setPatientName(String v) { patientName = v; }
    public int       getDoctorId()        { return doctorId; }
    public void      setDoctorId(int v)   { doctorId = v; }
    public String    getDoctorName()      { return doctorName; }
    public void      setDoctorName(String v) { doctorName = v; }
    public LocalDate getApptDate()        { return apptDate; }
    public void      setApptDate(LocalDate v) { apptDate = v; }
    public String    getApptTime()        { return apptTime; }
    public void      setApptTime(String v){ apptTime = v; }
    public String    getReason()          { return reason; }
    public void      setReason(String v)  { reason = v; }
    public String    getStatus()          { return status; }
    public void      setStatus(String v)  { status = v; }
}
