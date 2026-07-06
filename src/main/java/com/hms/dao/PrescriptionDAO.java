package com.hms.dao;

import com.hms.model.Prescription;
import com.hms.model.PrescriptionItem;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    public void addPrescriptionWithItems(Prescription presc, List<PrescriptionItem> items) throws SQLException {
        String insertPresc = "INSERT INTO Prescriptions(visit_id, patient_id, doctor_id, presc_date, notes) VALUES(?, ?, ?, ?, ?)";
        String insertItem = "INSERT INTO Prescription_Items(presc_id, medicine_id, dosage, frequency, duration_days, quantity) VALUES(seq_presc.currval, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Transaction

            // 1. Insert Prescription
            try (PreparedStatement pstmt = conn.prepareStatement(insertPresc)) {
                pstmt.setInt(1, presc.getVisitId());
                pstmt.setInt(2, presc.getPatientId());
                pstmt.setInt(3, presc.getDoctorId());
                pstmt.setDate(4, presc.getPrescDate());
                pstmt.setString(5, presc.getNotes());
                pstmt.executeUpdate();
            }

            // 2. Insert Items referencing seq_presc.currval
            try (PreparedStatement pstmtItem = conn.prepareStatement(insertItem)) {
                for (PrescriptionItem item : items) {
                    pstmtItem.setInt(1, item.getMedicineId());
                    pstmtItem.setString(2, item.getDosage());
                    pstmtItem.setString(3, item.getFrequency());
                    if (item.getDurationDays() != null) {
                        pstmtItem.setInt(4, item.getDurationDays());
                    } else {
                        pstmtItem.setNull(4, Types.INTEGER);
                    }
                    pstmtItem.setInt(5, item.getQuantity());
                    pstmtItem.addBatch();
                }
                pstmtItem.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            if (conn != null) conn.close();
        }
    }

    public List<Prescription> getAllPrescriptions() throws SQLException {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT * FROM Prescriptions ORDER BY presc_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Prescription p = new Prescription();
                p.setPrescId(rs.getInt("presc_id"));
                p.setVisitId(rs.getInt("visit_id"));
                p.setPatientId(rs.getInt("patient_id"));
                p.setDoctorId(rs.getInt("doctor_id"));
                p.setPrescDate(rs.getDate("presc_date"));
                p.setNotes(rs.getString("notes"));
                list.add(p);
            }
        }
        return list;
    }
}
