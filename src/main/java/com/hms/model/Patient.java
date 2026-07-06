package com.hms.model;

import java.time.LocalDate;

public class Patient {
    private int    patientId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String phone;
    private String email;
    private String address;
    private String emergencyContact;
    private String status;

    // Constructors
    public Patient() {}

    public Patient(int patientId, String firstName, String lastName,
                   LocalDate dateOfBirth, String gender, String bloodGroup,
                   String phone, String email, String address,
                   String emergencyContact, String status) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.status = status;
    }

    public String getFullName() { return firstName + " " + lastName; }

    // Getters & Setters
    public int    getPatientId()          { return patientId; }
    public void   setPatientId(int v)     { patientId = v; }
    public String getFirstName()          { return firstName; }
    public void   setFirstName(String v)  { firstName = v; }
    public String getLastName()           { return lastName; }
    public void   setLastName(String v)   { lastName = v; }
    public LocalDate getDateOfBirth()     { return dateOfBirth; }
    public void   setDateOfBirth(LocalDate v) { dateOfBirth = v; }
    public String getGender()             { return gender; }
    public void   setGender(String v)     { gender = v; }
    public String getBloodGroup()         { return bloodGroup; }
    public void   setBloodGroup(String v) { bloodGroup = v; }
    public String getPhone()              { return phone; }
    public void   setPhone(String v)      { phone = v; }
    public String getEmail()              { return email; }
    public void   setEmail(String v)      { email = v; }
    public String getAddress()            { return address; }
    public void   setAddress(String v)    { address = v; }
    public String getEmergencyContact()   { return emergencyContact; }
    public void   setEmergencyContact(String v) { emergencyContact = v; }
    public String getStatus()             { return status; }
    public void   setStatus(String v)     { status = v; }

    @Override
    public String toString() { return patientId + " - " + getFullName(); }
}
