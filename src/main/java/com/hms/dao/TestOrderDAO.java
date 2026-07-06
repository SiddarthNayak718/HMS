package com.hms.dao;

import com.hms.model.TestOrder;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestOrderDAO {

    public void addTestOrder(TestOrder to) throws SQLException {
        String sql = "INSERT INTO Test_Orders(visit_id, patient_id, test_id, ordered_by, order_date, status) VALUES(?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, to.getVisitId());
            pstmt.setInt(2, to.getPatientId());
            pstmt.setInt(3, to.getTestId());
            pstmt.setInt(4, to.getOrderedBy());
            pstmt.setDate(5, to.getOrderDate());
            pstmt.executeUpdate();
        }
    }

    public void updateResult(int orderId, String resultValue) throws SQLException {
        String sql = "UPDATE Test_Orders SET result_value = ?, result_date = SYSDATE, status = 'COMPLETED' WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resultValue);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        }
    }

    public List<TestOrder> getAllTestOrders() throws SQLException {
        List<TestOrder> list = new ArrayList<>();
        String sql = "SELECT t.*, lt.test_name, p.first_name || ' ' || p.last_name as patient_name " +
                     "FROM Test_Orders t " +
                     "JOIN Lab_Tests lt ON t.test_id = lt.test_id " +
                     "JOIN Patients p ON t.patient_id = p.patient_id " +
                     "ORDER BY t.order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                TestOrder to = new TestOrder();
                to.setOrderId(rs.getInt("order_id"));
                to.setVisitId(rs.getInt("visit_id"));
                to.setPatientId(rs.getInt("patient_id"));
                to.setTestId(rs.getInt("test_id"));
                to.setOrderedBy(rs.getInt("ordered_by"));
                to.setOrderDate(rs.getDate("order_date"));
                to.setResultValue(rs.getString("result_value"));
                to.setResultDate(rs.getDate("result_date"));
                to.setStatus(rs.getString("status"));
                to.setTestName(rs.getString("test_name"));
                to.setPatientName(rs.getString("patient_name"));
                list.add(to);
            }
        }
        return list;
    }
}
