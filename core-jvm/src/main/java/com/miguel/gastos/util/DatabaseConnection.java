package com.miguel.gastos.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String SQLITE_DB_NAME = "control_gastos.db";
    private static Connection connection = null;

    private DatabaseConnection() {}

    private static String getDbPath() {
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, ".control-gastos");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, SQLITE_DB_NAME).getAbsolutePath();
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = "jdbc:sqlite:" + getDbPath();
            connection = DriverManager.getConnection(url);
            initializeSchema();
        }
        return connection;
    }

    private static void initializeSchema() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS gastos (id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT NOT NULL, descripcion TEXT NOT NULL, monto REAL NOT NULL, fecha TEXT NOT NULL, dia_pago INTEGER, subcategoria TEXT, es_recurrente INTEGER);";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
