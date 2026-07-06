package com.hms.dao;

import com.hms.model.Bill;
import com.hms.model.Medicine;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// ============================================================
// BillDAO
// ============================================================
public class BillDAO {

    public boolean insertBill(Bill b) {
        String sql = "INSERT INTO Bills(patient_id, visit_id, consultation_fee, " +
                     "medicine_amount, lab_amount, discount) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, b.getPatientId());
            if (b.getVisitId() > 0) ps.setInt(2, b.getVisitId()); else ps.setNull(2, Types.INTEGER);
            ps.setDouble(3, b.getConsultationFee());
            ps.setDouble(4, b.getMedicineAmount());
            ps.setDouble(5, b.getLabAmount());
            ps.setDouble(6, b.getDiscount());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BillDAO] Insert error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean recordPayment(int billId, double amount, String mode) {
        String sql = "INSERT INTO Payments(bill_id, amount_paid, payment_mode) VALUES(?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ps.setDouble(2, amount);
            ps.setString(3, mode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BillDAO] Payment error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql =
            "SELECT b.bill_id, b.patient_id, (p.first_name||' '||p.last_name) AS patient_name, " +
            "NVL(b.visit_id,0) AS visit_id, b.bill_date, b.consultation_fee, " +
            "b.medicine_amount, b.lab_amount, b.total_amount, b.discount, b.net_amount, b.status " +
            "FROM Bills b JOIN Patients p ON p.patient_id = b.patient_id " +
            "ORDER BY b.bill_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Bill b = new Bill();
                b.setBillId(rs.getInt("bill_id"));
                b.setPatientId(rs.getInt("patient_id"));
                b.setPatientName(rs.getString("patient_name"));
                b.setVisitId(rs.getInt("visit_id"));
                Date bd = rs.getDate("bill_date");
                b.setBillDate(bd != null ? bd.toLocalDate() : null);
                b.setConsultationFee(rs.getDouble("consultation_fee"));
                b.setMedicineAmount(rs.getDouble("medicine_amount"));
                b.setLabAmount(rs.getDouble("lab_amount"));
                b.setTotalAmount(rs.getDouble("total_amount"));
                b.setDiscount(rs.getDouble("discount"));
                b.setNetAmount(rs.getDouble("net_amount"));
                b.setStatus(rs.getString("status"));
                list.add(b);
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] GetAll error: " + e.getMessage());
        }
        return list;
    }

    public double getTodaysRevenue() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT NVL(SUM(amount_paid),0) FROM Payments " +
                 "WHERE TRUNC(payment_date)=TRUNC(SYSDATE)");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }
}
