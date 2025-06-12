package org.example.service;

import org.example.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class CalendarioService {
    private static final String DB_URL = "jdbc:sqlite:taskmanager.db";

    public CalendarioService() {
        inicializarBaseDatos();
    }

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void inicializarBaseDatos() {
        try (Connection conn = conectar()) {
            // Crear tabla usuarios
            String sqlUsuarios = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    contrasena TEXT NOT NULL
                )
                """;

            // Crear tabla tareas
            String sqlTareas = """
                CREATE TABLE IF NOT EXISTS tareas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    descripcion TEXT,
                    fecha_inicio DATE NOT NULL,
                    fecha_fin DATE NOT NULL,
                    prioridad TEXT NOT NULL,
                    estado TEXT NOT NULL DEFAULT 'PENDIENTE',
                    recordatorio DATE,
                    usuario_id INTEGER,
                    FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
                )
                """;

            Statement stmt = conn.createStatement();
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlTareas);

            System.out.println("✅ Base de datos inicializada correctamente");

        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar base de datos: " + e.getMessage());
        }
    }

    // ==================== MÉTODOS PARA USUARIOS ====================

    /**
     * Verifica si ya existe un usuario con el email proporcionado
     */
    public boolean existeUsuario(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al verificar usuario existente: " + e.getMessage());
        }
        return false;
    }

    public boolean crearUsuario(Usuario usuario) {
        // Verificar si el usuario ya existe
        if (existeUsuario(usuario.getEmail())) {
            System.err.println("❌ Error: Ya existe un usuario con el email: " + usuario.getEmail());
            return false;
        }

        String sql = "INSERT INTO usuarios (nombre, email, contrasena) VALUES (?, ?, ?)";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setString(3, usuario.getContrasena());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    // Actualizar el ID del usuario con el generado por la BD
                     usuario.setId(generatedKeys.getInt(1)); // Si agregas setter para ID
                }
                System.out.println("✅ Usuario creado exitosamente: " + usuario.getNombre());
                return true;
            }

        } catch (SQLException e) {
            // Manejo específico para constraint de email único
            if (e.getMessage().contains("UNIQUE constraint failed: usuarios.email")) {
                System.err.println("❌ Error: El email " + usuario.getEmail() + " ya está registrado");
            } else {
                System.err.println("❌ Error al crear usuario: " + e.getMessage());
            }
        }
        return false;
    }

    public Usuario autenticarUsuario(String email, String contrasena) {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND contrasena = ?";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, contrasena);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario(
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("contrasena")
                );
                System.out.println("✅ Usuario autenticado: " + usuario.getNombre());
                return usuario;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error en autenticación: " + e.getMessage());
        }

        System.out.println("❌ Credenciales inválidas");
        return null;
    }

    // ==================== MÉTODOS PARA TAREAS ====================

    public boolean crearTarea(Tarea tarea, String emailUsuario) {
        // Primero obtener el ID del usuario
        int usuarioId = obtenerIdUsuario(emailUsuario);
        if (usuarioId == -1) {
            System.err.println("❌ Usuario no encontrado: " + emailUsuario);
            return false;
        }

        String sql = """
            INSERT INTO tareas (nombre, descripcion, fecha_inicio, fecha_fin, 
                               prioridad, estado, recordatorio, usuario_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, tarea.getNombre());
            pstmt.setString(2, tarea.getDescripcion());
            pstmt.setDate(3, new java.sql.Date(tarea.getFechaInicio().getTime()));
            pstmt.setDate(4, new java.sql.Date(tarea.getFechaFin().getTime()));
            pstmt.setString(5, tarea.getPrioridad().name());
            pstmt.setString(6, tarea.getEstado().name());
            pstmt.setDate(7, tarea.getRecordatorio() != null ? new java.sql.Date(tarea.getRecordatorio().getTime()) : null);
            pstmt.setInt(8, usuarioId);

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    tarea.setId(generatedKeys.getInt(1));
                }
                System.out.println("✅ Tarea creada: " + tarea.getNombre());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al crear tarea: " + e.getMessage());
        }
        return false;
    }

    public List<Tarea> obtenerTareas(String emailUsuario) {
        List<Tarea> tareas = new ArrayList<>();
        int usuarioId = obtenerIdUsuario(emailUsuario);

        if (usuarioId == -1) return tareas;

        String sql = "SELECT * FROM tareas WHERE usuario_id = ? ORDER BY fecha_inicio";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Tarea tarea = new Tarea(
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        new Date(rs.getDate("fecha_inicio").getTime()),
                        new Date(rs.getDate("fecha_fin").getTime()),
                        Prioridad.valueOf(rs.getString("prioridad")),
                        rs.getDate("recordatorio") != null ?
                                new Date(rs.getDate("recordatorio").getTime()) : null
                );

                tarea.setId(rs.getInt("id"));
                tarea.setEstado(Estado.valueOf(rs.getString("estado")));

                tareas.add(tarea);
            }

            System.out.println("✅ Cargadas " + tareas.size() + " tareas");

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener tareas: " + e.getMessage());
        }

        return tareas;
    }

    public boolean actualizarTarea(Tarea tarea) {
        String sql = """
            UPDATE tareas SET nombre = ?, descripcion = ?, fecha_inicio = ?, 
                             fecha_fin = ?, prioridad = ?, estado = ?, recordatorio = ?
            WHERE id = ?
            """;

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tarea.getNombre());
            pstmt.setString(2, tarea.getDescripcion());
            pstmt.setDate(3, new java.sql.Date(tarea.getFechaInicio().getTime()));
            pstmt.setDate(4, new java.sql.Date(tarea.getFechaFin().getTime()));
            pstmt.setString(5, tarea.getPrioridad().name());
            pstmt.setString(6, tarea.getEstado().name());
            pstmt.setDate(7, tarea.getRecordatorio() != null ?
                    new java.sql.Date(tarea.getRecordatorio().getTime()) : null);
            pstmt.setInt(8, tarea.getId());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("✅ Tarea actualizada: " + tarea.getNombre());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar tarea: " + e.getMessage());
        }
        return false;
    }

    public boolean eliminarTarea(int id) {
        String sql = "DELETE FROM tareas WHERE id = ?";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("✅ Tarea eliminada con ID: " + id);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar tarea: " + e.getMessage());
        }
        return false;
    }

    public List<Tarea> buscarTareas(String emailUsuario, String termino) {
        List<Tarea> tareas = new ArrayList<>();
        int usuarioId = obtenerIdUsuario(emailUsuario);

        if (usuarioId == -1) return tareas;

        String sql = """
            SELECT * FROM tareas 
            WHERE usuario_id = ? AND (nombre LIKE ? OR descripcion LIKE ?)
            ORDER BY fecha_inicio
            """;

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            pstmt.setString(2, "%" + termino + "%");
            pstmt.setString(3, "%" + termino + "%");

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Tarea tarea = new Tarea(
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        new Date(rs.getDate("fecha_inicio").getTime()),
                        new Date(rs.getDate("fecha_fin").getTime()),
                        Prioridad.valueOf(rs.getString("prioridad")),
                        rs.getDate("recordatorio") != null ?
                                new Date(rs.getDate("recordatorio").getTime()) : null
                );

                tarea.setId(rs.getInt("id"));
                tarea.setEstado(Estado.valueOf(rs.getString("estado")));

                tareas.add(tarea);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error en búsqueda: " + e.getMessage());
        }

        return tareas;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private int obtenerIdUsuario(String email) {
        String sql = "SELECT id FROM usuarios WHERE email = ?";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener ID usuario: " + e.getMessage());
        }
        return -1;
    }

    public boolean cambiarEstadoTarea(int id, Estado nuevoEstado) {
        String sql = "UPDATE tareas SET estado = ? WHERE id = ?";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado.name());
            pstmt.setInt(2, id);

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("✅ Estado de tarea actualizado: " + id);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al cambiar estado de tarea: " + e.getMessage());
        }

        return false;
    }

    // Metodo para obtener estadísticas
    public void mostrarEstadisticas(String emailUsuario) {
        int usuarioId = obtenerIdUsuario(emailUsuario);
        if (usuarioId == -1) return;

        String sql = """
            SELECT estado, COUNT(*) as cantidad 
            FROM tareas 
            WHERE usuario_id = ? 
            GROUP BY estado
            """;

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n📊 === ESTADÍSTICAS DE TAREAS ===");
            while (rs.next()) {
                System.out.printf("   %s: %d tareas\n",
                        rs.getString("estado"), rs.getInt("cantidad"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener estadísticas: " + e.getMessage());
        }
    }
}