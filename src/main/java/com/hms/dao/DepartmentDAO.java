package com.hms.dao;

import com.hms.model.Department;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT dept_id, dept_name, description, location FROM Departments ORDER BY dept_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Department(
                    rs.getInt("dept_id"),
                    rs.getString("dept_name"),
                    rs.getString("description"),
                    rs.getString("location")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DeptDAO] Error: " + e.getMessage());
        }
        return list;
    }

    public boolean insertDepartment(Department d) {
        String sql = "INSERT INTO Departments(dept_name, description, location) VALUES(?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getDeptName());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getLocation());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DeptDAO] Insert error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
