package com.hms.dao;

import com.hms.model.Patient;
import com.hms.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    private static final String INSERT_SQL =
        "INSERT INTO Patients(first_name, last_name, date_of_birth, gender, " +
        "blood_group, phone, email, address, emergency_contact, status) " +
        "VALUES(?,?,?,?,?,?,?,?,?,'ACTIVE')";

    private static final String UPDATE_SQL =
        "UPDATE Patients SET first_name=?, last_name=?, date_of_birth=?, gender=?, " +
        "blood_group=?, phone=?, email=?, address=?, emergency_contact=?, status=? " +
        "WHERE patient_id=?";

    private static final String DELETE_SQL =
        "UPDATE Patients SET status='INACTIVE' WHERE patient_id=?";

    private static final String SELECT_ALL_SQL =
        "SELECT patient_id, first_name, last_name, date_of_birth, gender, " +
        "blood_group, phone, email, address, emergency_contact, status " +
        "FROM Patients ORDER BY patient_id DESC";

    private static final String SELECT_BY_ID_SQL =
        "SELECT patient_id, first_name, last_name, date_of_birth, gender, " +
        "blood_group, phone, email, address, emergency_contact, status " +
        "FROM Patients WHERE patient_id=?";

    private static final String SEARCH_SQL =
        "SELECT patient_id, first_name, last_name, date_of_birth, gender, " +
        "blood_group, phone, email, address, emergency_contact, status " +
        "FROM Patients WHERE LOWER(first_name||' '||last_name) LIKE ? OR phone LIKE ? " +
        "ORDER BY patient_id DESC";

    public boolean insertPatient(Patient p) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setDate(3, Date.valueOf(p.getDateOfBirth()));
            ps.setString(4, p.getGender());
            ps.setString(5, p.getBloodGroup());
            ps.setString(6, p.getPhone());
            ps.setString(7, p.getEmail());
            ps.setString(8, p.getAddress());
            ps.setString(9, p.getEmergencyContact());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Insert error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean updatePatient(Patient p) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setDate(3, Date.valueOf(p.getDateOfBirth()));
            ps.setString(4, p.getGender());
            ps.setString(5, p.getBloodGroup());
            ps.setString(6, p.getPhone());
            ps.setString(7, p.getEmail());
            ps.setString(8, p.getAddress());
            ps.setString(9, p.getEmergencyContact());
            ps.setString(10, p.getStatus());
            ps.setInt(11, p.getPatientId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Update error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean deletePatient(int patientId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Delete error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[PatientDAO] GetAll error: " + e.getMessage());
        }
        return list;
    }

    public Patient getPatientById(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] GetById error: " + e.getMessage());
        }
        return null;
    }

    public List<Patient> searchPatients(String keyword) {
        List<Patient> list = new ArrayList<>();
        String like = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SEARCH_SQL)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Search error: " + e.getMessage());
        }
        return list;
    }

    public int getTotalPatients() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM Patients WHERE status='ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        Date dob = rs.getDate("date_of_birth");
        p.setDateOfBirth(dob != null ? dob.toLocalDate() : null);
        p.setGender(rs.getString("gender"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        p.setStatus(rs.getString("status"));
        return p;
    }
}
