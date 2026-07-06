package com.hms.dao;

import com.hms.model.Appointment;
import com.hms.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    private static final String BASE_SELECT =
        "SELECT a.appt_id, a.patient_id, " +
        "(p.first_name||' '||p.last_name) AS patient_name, " +
        "a.doctor_id, ('Dr. '||d.first_name||' '||d.last_name) AS doctor_name, " +
        "a.appt_date, a.appt_time, a.reason, a.status " +
        "FROM Appointments a " +
        "JOIN Patients p ON p.patient_id = a.patient_id " +
        "JOIN Doctors d ON d.doctor_id = a.doctor_id ";

    public boolean insertAppointment(Appointment a) {
        String sql = "INSERT INTO Appointments(patient_id, doctor_id, appt_date, " +
                     "appt_time, reason) VALUES(?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getPatientId());
            ps.setInt(2, a.getDoctorId());
            ps.setDate(3, Date.valueOf(a.getApptDate()));
            ps.setString(4, a.getApptTime());
            ps.setString(5, a.getReason());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ApptDAO] Insert error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean updateStatus(int apptId, String status) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE Appointments SET status=? WHERE appt_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, apptId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ApptDAO] UpdateStatus error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Appointment> getAllAppointments() {
        return queryList(BASE_SELECT + "ORDER BY a.appt_date DESC, a.appt_time");
    }

    public List<Appointment> getTodaysAppointments() {
        return queryList(BASE_SELECT + "WHERE TRUNC(a.appt_date)=TRUNC(SYSDATE) " +
                         "ORDER BY a.appt_time");
    }

    public List<Appointment> getByPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE a.patient_id=? ORDER BY a.appt_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ApptDAO] GetByPatient error: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getByDoctor(int doctorId, LocalDate date) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE a.doctor_id=? AND TRUNC(a.appt_date)=? " +
                     "ORDER BY a.appt_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ApptDAO] GetByDoctor error: " + e.getMessage());
        }
        return list;
    }

    public int getTodaysCount() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM Appointments WHERE TRUNC(appt_date)=TRUNC(SYSDATE)");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    private List<Appointment> queryList(String sql) {
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ApptDAO] Query error: " + e.getMessage());
        }
        return list;
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setApptId(rs.getInt("appt_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setPatientName(rs.getString("patient_name"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setDoctorName(rs.getString("doctor_name"));
        Date d = rs.getDate("appt_date");
        a.setApptDate(d != null ? d.toLocalDate() : null);
        a.setApptTime(rs.getString("appt_time"));
        a.setReason(rs.getString("reason"));
        a.setStatus(rs.getString("status"));
        return a;
    }
}
