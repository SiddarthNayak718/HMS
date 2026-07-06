package com.hms.model;

import java.time.LocalDate;

public class Doctor {
    private int    doctorId;
    private String firstName;
    private String lastName;
    private String specialization;
    private int    deptId;
    private String deptName;
    private String phone;
    private String email;
    private String qualification;
    private LocalDate joiningDate;
    private String status;

    public Doctor() {}

    public Doctor(int doctorId, String firstName, String lastName,
                  String specialization, int deptId, String deptName,
                  String phone, String email, String qualification,
                  LocalDate joiningDate, String status) {
        this.doctorId = doctorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.deptId = deptId;
        this.deptName = deptName;
        this.phone = phone;
        this.email = email;
        this.qualification = qualification;
        this.joiningDate = joiningDate;
        this.status = status;
    }

    public String getFullName() { return "Dr. " + firstName + " " + lastName; }

    // Getters & Setters
    public int    getDoctorId()             { return doctorId; }
    public void   setDoctorId(int v)        { doctorId = v; }
    public String getFirstName()            { return firstName; }
    public void   setFirstName(String v)    { firstName = v; }
    public String getLastName()             { return lastName; }
    public void   setLastName(String v)     { lastName = v; }
    public String getSpecialization()       { return specialization; }
    public void   setSpecialization(String v) { specialization = v; }
    public int    getDeptId()               { return deptId; }
    public void   setDeptId(int v)          { deptId = v; }
    public String getDeptName()             { return deptName; }
    public void   setDeptName(String v)     { deptName = v; }
    public String getPhone()                { return phone; }
    public void   setPhone(String v)        { phone = v; }
    public String getEmail()                { return email; }
    public void   setEmail(String v)        { email = v; }
    public String getQualification()        { return qualification; }
    public void   setQualification(String v){ qualification = v; }
    public LocalDate getJoiningDate()       { return joiningDate; }
    public void   setJoiningDate(LocalDate v){ joiningDate = v; }
    public String getStatus()               { return status; }
    public void   setStatus(String v)       { status = v; }

    @Override
    public String toString() { return getFullName() + " (" + specialization + ")"; }
}
