package com.hms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection manager for Oracle Database.
 * Update DB_URL, USER, and PASSWORD to match your environment.
 */
public class DBConnection {

    // -------------------------------------------------------
    // Configure these for your Oracle instance
    // -------------------------------------------------------
    private static final String DB_URL  = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
    private static final String DB_USER = "hms_user";
    private static final String DB_PASS = "hms_password";   // your Oracle password
    // -------------------------------------------------------

    private static Connection connection;

    private DBConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                connection.setAutoCommit(true);
                System.out.println("[DB] Connected to Oracle successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Oracle JDBC driver not found. Add ojdbc8.jar to classpath.");
            throw new RuntimeException("Oracle driver missing", e);
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            throw new RuntimeException("DB connection failed", e);
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
