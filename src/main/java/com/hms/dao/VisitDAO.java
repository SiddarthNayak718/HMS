package com.hms.dao;

import com.hms.model.Visit;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitDAO {

    public void addVisit(Visit visit) throws SQLException {
        String sql = "INSERT INTO Visits(appt_id, patient_id, doctor_id, visit_date, diagnosis, notes, follow_up_date) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (visit.getApptId() != null) {
                pstmt.setInt(1, visit.getApptId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setInt(2, visit.getPatientId());
            pstmt.setInt(3, visit.getDoctorId());
            pstmt.setDate(4, visit.getVisitDate());
            pstmt.setString(5, visit.getDiagnosis());
            pstmt.setString(6, visit.getNotes());
            pstmt.setDate(7, visit.getFollowUpDate());
            pstmt.executeUpdate();
        }
    }

    public List<Visit> getAllVisits() throws SQLException {
        List<Visit> list = new ArrayList<>();
        String sql = "SELECT * FROM Visits ORDER BY visit_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Visit v = new Visit();
                v.setVisitId(rs.getInt("visit_id"));
                v.setApptId(rs.getInt("appt_id"));
                if (rs.wasNull()) v.setApptId(null);
                v.setPatientId(rs.getInt("patient_id"));
                v.setDoctorId(rs.getInt("doctor_id"));
                v.setVisitDate(rs.getDate("visit_date"));
                v.setDiagnosis(rs.getString("diagnosis"));
                v.setNotes(rs.getString("notes"));
                v.setFollowUpDate(rs.getDate("follow_up_date"));
                list.add(v);
            }
        }
        return list;
    }
}
