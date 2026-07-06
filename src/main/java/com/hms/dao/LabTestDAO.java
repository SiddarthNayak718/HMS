package com.hms.dao;

import com.hms.model.LabTest;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LabTestDAO {

    public List<LabTest> getAllLabTests() throws SQLException {
        List<LabTest> list = new ArrayList<>();
        String sql = "SELECT * FROM Lab_Tests";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LabTest test = new LabTest();
                test.setTestId(rs.getInt("test_id"));
                test.setTestName(rs.getString("test_name"));
                test.setDescription(rs.getString("description"));
                test.setPrice(rs.getDouble("price"));
                test.setNormalRange(rs.getString("normal_range"));
                list.add(test);
            }
        }
        return list;
    }
}
