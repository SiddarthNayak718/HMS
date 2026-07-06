package com.hms.dao;

import com.hms.model.Medicine;
import com.hms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    private static final String INSERT_SQL =
        "INSERT INTO Medicines(name, category, unit_price, stock_qty, manufacturer, expiry_date) " +
        "VALUES(?,?,?,?,?,?)";

    private static final String UPDATE_SQL =
        "UPDATE Medicines SET name=?, category=?, unit_price=?, stock_qty=?, " +
        "manufacturer=?, expiry_date=? WHERE medicine_id=?";

    public boolean insertMedicine(Medicine m) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getCategory());
            ps.setDouble(3, m.getUnitPrice());
            ps.setInt(4, m.getStockQty());
            ps.setString(5, m.getManufacturer());
            if (m.getExpiryDate() != null)
                ps.setDate(6, Date.valueOf(m.getExpiryDate()));
            else ps.setNull(6, Types.DATE);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MedicineDAO] Insert error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean updateMedicine(Medicine m) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getCategory());
            ps.setDouble(3, m.getUnitPrice());
            ps.setInt(4, m.getStockQty());
            ps.setString(5, m.getManufacturer());
            if (m.getExpiryDate() != null)
                ps.setDate(6, Date.valueOf(m.getExpiryDate()));
            else ps.setNull(6, Types.DATE);
            ps.setInt(7, m.getMedicineId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MedicineDAO] Update error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT medicine_id, name, category, unit_price, stock_qty, " +
                     "manufacturer, expiry_date FROM Medicines ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[MedicineDAO] GetAll error: " + e.getMessage());
        }
        return list;
    }

    public List<Medicine> getLowStock(int threshold) {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT medicine_id, name, category, unit_price, stock_qty, " +
                     "manufacturer, expiry_date FROM Medicines WHERE stock_qty <= ? ORDER BY stock_qty";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { /* ignore */ }
        return list;
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getInt("medicine_id"));
        m.setName(rs.getString("name"));
        m.setCategory(rs.getString("category"));
        m.setUnitPrice(rs.getDouble("unit_price"));
        m.setStockQty(rs.getInt("stock_qty"));
        m.setManufacturer(rs.getString("manufacturer"));
        Date exp = rs.getDate("expiry_date");
        m.setExpiryDate(exp != null ? exp.toLocalDate() : null);
        return m;
    }
}
