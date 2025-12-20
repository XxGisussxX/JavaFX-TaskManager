package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.example.model.*;
import org.example.service.CalendarioService;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class CrearTareaController implements Initializable {

    // ==================== COMPONENTES FXML ====================

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private ComboBox<Prioridad> cbPrioridad;
    @FXML private ComboBox<Estado> cbEstado;
    @FXML private DatePicker dpRecordatorio;
    @FXML private Button btnLimpiarRecordatorio;
    @FXML private Label lblMensaje;
    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private ListView<String> listTareas;

    // ==================== PROPIEDADES ====================

    private CalendarioService calendarioService;
    private String emailUsuarioActual = "admin@test.com"; // Temporal, luego vendrá del login
    private Tarea tareaEnEdicion = null; // Para modo edición
    private ObservableList<String> listaTareasObservable;
    private CalendarioController calendarioController;
    private Usuario usuarioActual;

    // ==================== INICIALIZACIÓN ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        calendarioService = new CalendarioService();
        listaTareasObservable = FXCollections.observableArrayList();

        configurarComponentes();
        cargarDatosIniciales();
        configurarValidaciones();
        cargarTareasEnLista();

        System.out.println("✅ CrearTareaController inicializado");
    }

    private void configurarComponentes() {
        // Configurar ComboBox de Prioridad
        cbPrioridad.setItems(FXCollections.observableArrayList(Prioridad.values()));
        cbPrioridad.setConverter(new StringConverter<Prioridad>() {
            @Override
            public String toString(Prioridad prioridad) {
                return prioridad != null ? prioridad.getDescripcion() : "";
            }

            @Override
            public Prioridad fromString(String string) {
                for (Prioridad p : Prioridad.values()) {
                    if (p.getDescripcion().equals(string)) {
                        return p;
                    }
                }
                return null;
            }
        });

        // Configurar ComboBox de Estado
        cbEstado.setItems(FXCollections.observableArrayList(Estado.values()));
        cbEstado.setConverter(new StringConverter<Estado>() {
            @Override
            public String toString(Estado estado) {
                return estado != null ? estado.getDescripcion() : "";
            }

            @Override
            public Estado fromString(String string) {
                for (Estado e : Estado.values()) {
                    if (e.getDescripcion().equals(string)) {
                        return e;
                    }
                }
                return null;
            }
        });

        // Configurar ListView
        listTareas.setItems(listaTareasObservable);

        // Configurar DatePickers con valores por defecto
        dpFechaInicio.setValue(LocalDate.now());
        dpFechaFin.setValue(LocalDate.now().plusDays(1));
    }

    private void cargarDatosIniciales() {
        // Valores por defecto
        cbPrioridad.setValue(Prioridad.IMPORTANTE);
        cbEstado.setValue(Estado.PENDIENTE);

        // ❌ Ya no se necesita crear usuario aquí
        // ✅ Este controlador ahora solo usa el email desde SesionUsuario
    }


    private void configurarValidaciones() {
        // Validación en tiempo real para fechas
        dpFechaInicio.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpFechaFin.getValue() != null && newVal.isAfter(dpFechaFin.getValue())) {
                mostrarMensaje("⚠️ La fecha de inicio no puede ser posterior a la fecha de fin", Color.ORANGE);
                dpFechaFin.setValue(newVal.plusDays(1));
            }
        });

        dpFechaFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpFechaInicio.getValue() != null && newVal.isBefore(dpFechaInicio.getValue())) {
                mostrarMensaje("⚠️ La fecha de fin no puede ser anterior a la fecha de inicio", Color.ORANGE);
                dpFechaInicio.setValue(newVal.minusDays(1));
            }
        });

        // Validación de nombre requerido
        txtNombre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.trim().length() > 50) {
                txtNombre.setText(oldVal); // Limitar a 50 caracteres
                mostrarMensaje("⚠️ El nombre no puede exceder 50 caracteres", Color.ORANGE);
            }
        });
    }

    // ==================== EVENTOS DE BOTONES ====================

    @FXML
    private void guardarTarea() {
        if (!validarFormulario()) {
            return;
        }

        try {
            Tarea tarea = construirTareaDesdeFormulario();
            boolean exito;

            if (tareaEnEdicion != null) {
                // Modo edición
                tarea.setId(tareaEnEdicion.getId());
                exito = calendarioService.actualizarTarea(tarea);

                if (exito) {
                    mostrarMensaje("✅ Tarea actualizada correctamente", Color.GREEN);
                    System.out.println("✅ Tarea actualizada exitosamente");
                } else {
                    mostrarMensaje("❌ Error al actualizar tarea", Color.RED);
                    System.err.println("❌ Error al actualizar tarea en el servicio");
                }
            } else {
                // Modo creación
                exito = calendarioService.crearTarea(tarea, emailUsuarioActual);

                if (exito) {
                    mostrarMensaje("✅ Tarea creada correctamente", Color.GREEN);
                    System.out.println("✅ Tarea creada exitosamente");
                } else {
                    mostrarMensaje("❌ Error al crear tarea", Color.RED);
                    System.err.println("❌ Error al crear tarea en el servicio");
                }
            }

            if (exito) {
                // Actualizar la lista inmediatamente después del guardado exitoso
                cargarTareasEnLista();

                // Notificar al controlador principal si existe
                if (calendarioController != null) {
                    try {
                        // Llama al metodo de actualización si existe
                        calendarioController.actualizarVistaTareas();
                    } catch (Exception e) {
                        // Si el metodo no existe, solo registra en log pero continúa
                        System.out.println("ℹ️ Método actualizarVistaTareas() no implementado en CalendarioController");
                    }
                }

                // Limpiar formulario solo si fue exitoso
                limpiarFormulario();
            }

        } catch (Exception e) {
            System.err.println("❌ Error inesperado al guardar tarea: " + e.getMessage());
            e.printStackTrace(); // Para ver el stack trace completo
            mostrarMensaje("❌ Error inesperado al guardar la tarea", Color.RED);
        }
    }

    @FXML
    private void limpiarFormulario() {
        // Solo limpiar sin mostrar mensaje si viene de un guardado exitoso
        limpiarFormularioInterno(false);
    }

    @FXML
    private void cancelar() {
        if (tareaEnEdicion != null || !formularioVacio()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Cancelación");
            alert.setHeaderText("¿Deseas cancelar?");
            alert.setContentText("Se perderán los cambios no guardados.");

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                limpiarFormularioInterno(true);
                mostrarMensaje("❌ Operación cancelada", Color.GRAY);
            }
        } else {
            limpiarFormularioInterno(true);
            mostrarMensaje("📋 Formulario reiniciado", Color.BLUE);
        }
    }

    @FXML
    private void limpiarRecordatorio() {
        dpRecordatorio.setValue(null);
        mostrarMensaje("🔔 Recordatorio eliminado", Color.BLUE);
    }

    // ==================== MÉTODOS AUXILIARES DE LIMPIEZA ====================

    /**
     * Metodo interno para limpiar el formulario
     * @param mostrarMensajeLimpieza Si debe mostrar mensaje de limpieza o no
     */
    private void limpiarFormularioInterno(boolean mostrarMensajeLimpieza) {
        txtNombre.clear();
        txtDescripcion.clear();
        dpFechaInicio.setValue(LocalDate.now());
        dpFechaFin.setValue(LocalDate.now().plusDays(1));
        cbPrioridad.setValue(Prioridad.IMPORTANTE);
        cbEstado.setValue(Estado.PENDIENTE);
        dpRecordatorio.setValue(null);

        tareaEnEdicion = null;
        lblTitulo.setText("📝 Crear Nueva Tarea");
        btnGuardar.setText("💾 Guardar");

        if (mostrarMensajeLimpieza) {
            mostrarMensaje("🧹 Formulario limpiado", Color.BLUE);
            System.out.println("🧹 Formulario limpiado por acción del usuario");
        } else {
            // Solo ocultar mensaje si no debe mostrar mensaje de limpieza
            lblMensaje.setVisible(false);
            System.out.println("🧹 Formulario reiniciado después de guardado exitoso");
        }

        txtNombre.requestFocus();
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        // Validar nombre
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre es obligatorio\n");
        }

        // Validar fechas
        if (dpFechaInicio.getValue() == null) {
            errores.append("• La fecha de inicio es obligatoria\n");
        }
        if (dpFechaFin.getValue() == null) {
            errores.append("• La fecha de fin es obligatoria\n");
        }

        if (dpFechaInicio.getValue() != null && dpFechaFin.getValue() != null) {
            if (dpFechaInicio.getValue().isAfter(dpFechaFin.getValue())) {
                errores.append("• La fecha de inicio no puede ser posterior a la fecha de fin\n");
            }
        }

        // Validar selecciones
        if (cbPrioridad.getValue() == null) {
            errores.append("• Debes seleccionar una prioridad\n");
        }
        if (cbEstado.getValue() == null) {
            errores.append("• Debes seleccionar un estado\n");
        }

        if (errores.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Formulario Incompleto");
            alert.setHeaderText("Por favor corrige los siguientes errores:");
            alert.setContentText(errores.toString());
            alert.showAndWait();

            mostrarMensaje("⚠️ Revisa los campos obligatorios", Color.ORANGE);
            return false;
        }

        return true;
    }

    private boolean formularioVacio() {
        return (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) &&
                (txtDescripcion.getText() == null || txtDescripcion.getText().trim().isEmpty()) &&
                dpFechaInicio.getValue().equals(LocalDate.now()) &&
                dpFechaFin.getValue().equals(LocalDate.now().plusDays(1)) &&
                dpRecordatorio.getValue() == null;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Tarea construirTareaDesdeFormulario() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";

        Date fechaInicio = convertirLocalDateADate(dpFechaInicio.getValue());
        Date fechaFin = convertirLocalDateADate(dpFechaFin.getValue());
        Date recordatorio = dpRecordatorio.getValue() != null ?
                convertirLocalDateADate(dpRecordatorio.getValue()) : null;

        Prioridad prioridad = cbPrioridad.getValue();

        Tarea tarea = new Tarea(nombre, descripcion, fechaInicio, fechaFin, prioridad, recordatorio);
        tarea.setEstado(cbEstado.getValue());

        return tarea;
    }

    private Date convertirLocalDateADate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate convertirDateALocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void mostrarMensaje(String mensaje, Color color) {
        lblMensaje.setText(mensaje);
        lblMensaje.setTextFill(color);
        lblMensaje.setVisible(true);

        // Autoocultar mensaje después de 5 segundos
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> lblMensaje.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void cargarTareasEnLista() {
        try {
            List<Tarea> tareas = calendarioService.obtenerTareas(SesionUsuario.emailActual);
            listaTareasObservable.clear();

            for (Tarea tarea : tareas) {
                String itemLista = String.format("ID:%d - %s [%s] (%s)",
                        tarea.getId(),
                        tarea.getNombre(),
                        tarea.getEstado().getDescripcion(),
                        tarea.getPrioridad().getDescripcion()
                );
                listaTareasObservable.add(itemLista);
            }

            System.out.println("📋 Lista actualizada con " + tareas.size() + " tareas");

        } catch (Exception e) {
            System.err.println("❌ Error al cargar tareas en lista: " + e.getMessage());
            e.printStackTrace(); // Para debugging
            mostrarMensaje("❌ Error al cargar la lista de tareas", Color.RED);
        }
    }

    // ==================== MÉTODOS PÚBLICOS PARA EDICIÓN ====================

    public void cargarTareaParaEdicion(Tarea tarea) {
        this.tareaEnEdicion = tarea;

        // Cambiar título y botón
        lblTitulo.setText("✏️ Editar Tarea");
        btnGuardar.setText("💾 Actualizar");

        // Cargar datos en el formulario
        txtNombre.setText(tarea.getNombre());
        txtDescripcion.setText(tarea.getDescripcion());
        dpFechaInicio.setValue(convertirDateALocalDate(tarea.getFechaInicio()));
        dpFechaFin.setValue(convertirDateALocalDate(tarea.getFechaFin()));
        cbPrioridad.setValue(tarea.getPrioridad());
        cbEstado.setValue(tarea.getEstado());

        if (tarea.getRecordatorio() != null) {
            dpRecordatorio.setValue(convertirDateALocalDate(tarea.getRecordatorio()));
        }

        mostrarMensaje("✏️ Modo edición activado para: " + tarea.getNombre(), Color.BLUE);
    }

    public void setEmailUsuario(String email) {
        this.emailUsuarioActual = email;
        cargarTareasEnLista();
    }

    // Setter para el controlador principal
    public void setCalendarioController(CalendarioController calendarioController) {
        this.calendarioController = calendarioController;
    }

    // Setter para el usuario actual
    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        if (usuarioActual != null && usuarioActual.getEmail() != null) {
            this.emailUsuarioActual = usuarioActual.getEmail();
            cargarTareasEnLista();
        }
    }

    // ==================== MÉTODOS PARA ESTADÍSTICAS ====================

    public void mostrarEstadisticas() {
        calendarioService.mostrarEstadisticas(emailUsuarioActual);
    }

    // ==================== METODO PÚBLICO PARA REFRESCAR LISTA ====================

    /**
     * Metodo público para actualizar la lista de tareas desde otros controladores
     */
    public void actualizarListaTareas() {
        cargarTareasEnLista();
    }
}