package org.example.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CalendarioService {
    private static final String DB_URL = "jdbc:sqlite:taskmanager.db";

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Aquí luego podrás crear métodos como:
    // - crearTarea(...)
    // - obtenerTareas(...)
    // - actualizarTarea(...)
    // - eliminarTarea(...)
}

