package com.hms.dao;

import com.hms.model.Doctor;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    private static final String INSERT_SQL =
        "INSERT INTO Doctors(first_name, last_name, specialization, dept_id, " +
        "phone, email, qualification) VALUES(?,?,?,?,?,?,?)";

    private static final String UPDATE_SQL =
        "UPDATE Doctors SET first_name=?, last_name=?, specialization=?, dept_id=?, " +
        "phone=?, email=?, qualification=?, status=? WHERE doctor_id=?";

    private static final String SELECT_ALL_SQL =
        "SELECT d.doctor_id, d.first_name, d.last_name, d.specialization, " +
        "d.dept_id, dep.dept_name, d.phone, d.email, d.qualification, " +
        "d.joining_date, d.status " +
        "FROM Doctors d LEFT JOIN Departments dep ON d.dept_id = dep.dept_id " +
        "ORDER BY d.doctor_id";

    private static final String SELECT_BY_ID_SQL =
        "SELECT d.doctor_id, d.first_name, d.last_name, d.specialization, " +
        "d.dept_id, dep.dept_name, d.phone, d.email, d.qualification, " +
        "d.joining_date, d.status " +
        "FROM Doctors d LEFT JOIN Departments dep ON d.dept_id = dep.dept_id " +
        "WHERE d.doctor_id=?";

    public boolean insertDoctor(Doctor doc) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, doc.getFirstName());
            ps.setString(2, doc.getLastName());
            ps.setString(3, doc.getSpecialization());
            ps.setInt(4, doc.getDeptId());
            ps.setString(5, doc.getPhone());
            ps.setString(6, doc.getEmail());
            ps.setString(7, doc.getQualification());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] Insert error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean updateDoctor(Doctor doc) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, doc.getFirstName());
            ps.setString(2, doc.getLastName());
            ps.setString(3, doc.getSpecialization());
            ps.setInt(4, doc.getDeptId());
            ps.setString(5, doc.getPhone());
            ps.setString(6, doc.getEmail());
            ps.setString(7, doc.getQualification());
            ps.setString(8, doc.getStatus());
            ps.setInt(9, doc.getDoctorId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] Update error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] GetAll error: " + e.getMessage());
        }
        return list;
    }

    public Doctor getDoctorById(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { /* ignore */ }
        return null;
    }

    public List<Doctor> getActiveDoctors() {
        List<Doctor> list = new ArrayList<>();
        String sql = SELECT_ALL_SQL.replace("ORDER BY d.doctor_id",
                     "WHERE d.status='ACTIVE' ORDER BY d.doctor_id");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] GetActive error: " + e.getMessage());
        }
        return list;
    }

    public int getTotalDoctors() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM Doctors WHERE status='ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("doctor_id"));
        d.setFirstName(rs.getString("first_name"));
        d.setLastName(rs.getString("last_name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setDeptId(rs.getInt("dept_id"));
        d.setDeptName(rs.getString("dept_name"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setQualification(rs.getString("qualification"));
        Date jd = rs.getDate("joining_date");
        d.setJoiningDate(jd != null ? jd.toLocalDate() : null);
        d.setStatus(rs.getString("status"));
        return d;
    }
}
