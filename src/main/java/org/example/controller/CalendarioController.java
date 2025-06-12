package org.example.controller;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DateControl;
import com.calendarfx.view.RequestEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.model.*;
import org.example.service.CalendarioService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CalendarioController {

    // ==================== COMPONENTES FXML ====================
    @FXML private Pane calendarioPane;
    @FXML private Button btnNuevaTarea;
    @FXML private Button btnVerTareas;
    @FXML private Button btnPerfil;
    @FXML private Button btnTogglePanel;
    @FXML private ComboBox<String> cbVistaCalendario;
    @FXML private TextField txtBuscar;
    @FXML private Label lblUsuario;
    @FXML private Label lblEstado;
    @FXML private Label lblFechaActual;
    @FXML private VBox panelLateral;
    @FXML private VBox panelCarga;
    @FXML private ListView<String> listaProximasTareas;

    // Estadísticas
    @FXML private Label lblPendientes;
    @FXML private Label lblEnProgreso;
    @FXML private Label lblCompletadas;
    @FXML private Label lblVencidas;

    // ==================== VARIABLES DE INSTANCIA ====================
    private CalendarView calendarView;
    private CalendarioService calendarioService;
    private Calendar calendarioTareas;
    private Usuario usuarioActual;
    private boolean panelLateralVisible = true;

    // ==================== INICIALIZACIÓN ====================
    @FXML
    public void initialize() {
        System.out.println("🔧 Inicializando CalendarioController...");

        // Inicializar servicios
        calendarioService = new CalendarioService();

        // Configurar fecha actual
        actualizarFechaActual();

        // Configurar calendario
        configurarCalendario();

        // Configurar lista de próximas tareas
        configurarListaProximasTareas();

        // Simular usuario por defecto (esto debería venir del login)
        simularUsuarioLogueado();

        // Cargar datos iniciales
        cargarTareasEnCalendario();
        actualizarEstadisticas();

        System.out.println("✅ CalendarioController inicializado correctamente");
    }

    // Este es el metodo que configura el calendario
    private void configurarCalendario() {
        // Crear el calendario principal
        calendarView = new CalendarView();

        // Configurar propiedades
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowToolBar(true);

        // Crear calendario de tareas
        calendarioTareas = new com.calendarfx.model.Calendar("Mis Tareas");
        calendarioTareas.setStyle(com.calendarfx.model.Calendar.Style.STYLE2);

        // Crear fuente de calendarios
        com.calendarfx.model.CalendarSource calendarios = new com.calendarfx.model.CalendarSource("Calendarios Personales");
        calendarios.getCalendars().add(calendarioTareas);

        // Agregar al calendario principal
        calendarView.getCalendarSources().add(calendarios);

        // Configurar eventos
        calendarView.setEntryDetailsPopOverContentCallback(this::mostrarDetallesTarea);
        calendarView.setEntryContextMenuCallback(this::crearMenuContextual);

        // (Si tienes este metodo, agrégalo)
        // calendarView.addEventHandler(RequestEvent.REQUEST_ENTRY, this::manejarNuevaEntrada);

        // Agregar al panel
        calendarioPane.getChildren().add(calendarView);

        // Hacer que el calendario ocupe el espacio disponible
        calendarView.prefWidthProperty().bind(calendarioPane.widthProperty());
        calendarView.prefHeightProperty().bind(calendarioPane.heightProperty());
    }

    // Metodo para mostrar detalles de la tarea
    private Node mostrarDetallesTarea(DateControl.EntryDetailsPopOverContentParameter param) {
        Entry<?> entry = param.getEntry();

        VBox detallesBox = new VBox(5);
        Label titulo = new Label("Título: " + entry.getTitle());
        Label descripcion = new Label("Descripción: " + entry.getLocation());

        detallesBox.getChildren().addAll(titulo, descripcion);
        return detallesBox;
    }



    // Metodo para crear el menú contextual de la tarea
    private ContextMenu crearMenuContextual(DateControl.EntryContextMenuParameter param) {
        Entry<?> entry = param.getEntry();

        ContextMenu menu = new ContextMenu();

        // Opción de eliminar la tarea
        MenuItem eliminar = new MenuItem("Eliminar");
        eliminar.setOnAction(e -> calendarioTareas.removeEntry(entry));

        // Opción de editar la tarea (puedes enlazarlo con tu lógica de edición)
        MenuItem editar = new MenuItem("Editar");
        editar.setOnAction(e -> {
            System.out.println("Editar tarea: " + entry.getTitle());
            // Aquí podrías abrir un formulario de edición si lo deseas
        });

        // Agregar las opciones al menú
        menu.getItems().addAll(eliminar, editar);

        return menu;
    }

    /**
     * Método público para actualizar completamente la vista de tareas
     * Este método puede ser llamado desde otros controladores (como CrearTareaController)
     * para refrescar la interfaz después de realizar cambios en las tareas
     */
    public void actualizarVistaTareas() {
        if (usuarioActual == null) {
            System.out.println("⚠️ No hay usuario actual para actualizar las tareas");
            return;
        }

        try {
            actualizarEstado("Actualizando vista de tareas...");

            // Mostrar panel de carga mientras se actualizan los datos
            mostrarPanelCarga(true);

            // Ejecutar actualización en el hilo de JavaFX
            Platform.runLater(() -> {
                try {
                    // 1. Recargar tareas en el calendario
                    cargarTareasEnCalendario();

                    // 2. Actualizar estadísticas (pendientes, en progreso, completadas, vencidas)
                    actualizarEstadisticas();

                    // 3. Actualizar lista de próximas tareas en el panel lateral
                    List<Tarea> tareas = calendarioService.obtenerTareas(usuarioActual.getEmail());
                    actualizarProximasTareas(tareas);

                    // 4. Limpiar campo de búsqueda si hay texto
                    if (txtBuscar != null && !txtBuscar.getText().trim().isEmpty()) {
                        txtBuscar.clear();
                    }

                    // 5. Actualizar estado final
                    actualizarEstado("Vista de tareas actualizada correctamente");

                    System.out.println("✅ Vista de tareas actualizada exitosamente");

                } catch (Exception e) {
                    System.err.println("❌ Error al actualizar vista de tareas: " + e.getMessage());
                    mostrarError("Error de Actualización",
                            "Ocurrió un error al actualizar la vista de tareas: " + e.getMessage());
                    actualizarEstado("Error al actualizar vista de tareas");
                } finally {
                    // Ocultar panel de carga
                    mostrarPanelCarga(false);
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error crítico en actualizarVistaTareas: " + e.getMessage());
            mostrarError("Error Crítico", "Error crítico al actualizar las tareas: " + e.getMessage());
        }
    }

    /**
     * Metodo sobrecargado que permite actualizar la vista con un mensaje personalizado
     * @param mensajePersonalizado Mensaje que se mostrará durante la actualización
     */
    public void actualizarVistaTareas(String mensajePersonalizado) {
        if (usuarioActual == null) {
            System.out.println("⚠️ No hay usuario actual para actualizar las tareas");
            return;
        }

        try {
            actualizarEstado(mensajePersonalizado);
            mostrarPanelCarga(true);

            Platform.runLater(() -> {
                try {
                    cargarTareasEnCalendario();
                    actualizarEstadisticas();

                    List<Tarea> tareas = calendarioService.obtenerTareas(usuarioActual.getEmail());
                    actualizarProximasTareas(tareas);

                    if (txtBuscar != null && !txtBuscar.getText().trim().isEmpty()) {
                        txtBuscar.clear();
                    }

                    actualizarEstado("Vista actualizada: " + mensajePersonalizado);
                    System.out.println("✅ " + mensajePersonalizado + " - Vista actualizada");

                } catch (Exception e) {
                    System.err.println("❌ Error al actualizar vista: " + e.getMessage());
                    mostrarError("Error de Actualización", e.getMessage());
                } finally {
                    mostrarPanelCarga(false);
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error crítico: " + e.getMessage());
            mostrarError("Error Crítico", e.getMessage());
        }
    }

    private void configurarListaProximasTareas() {
        listaProximasTareas.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setStyle("-fx-padding: 5; -fx-font-size: 11px;");
                        }
                    }
                };
            }
        });
    }

    // ==================== EVENTOS DEL CALENDARIO ====================
    private void manejarNuevaEntrada(RequestEvent event) {
        // Cuando el usuario hace doble clic en el calendario
        Platform.runLater(() -> {
            try {
                abrirFormularioTarea();
            } catch (Exception e) {
                mostrarError("Error al abrir formulario de tarea", e.getMessage());
            }
        });
    }

    private Callback<Entry<?>, Boolean> mostrarDetallesTarea = entry -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de la Tarea");
        alert.setHeaderText(entry.getTitle());
        alert.setContentText(
                "Inicio: " + entry.getStartDate() + "\n" +
                        "Fin: " + entry.getEndDate() + "\n" +
                        "Descripción: " + (entry.getLocation() != null ? entry.getLocation() : "Sin descripción")
        );
        alert.showAndWait();
        return true;
    };

    private Callback<Entry<?>, ContextMenu> crearMenuContextual = entry -> {
        ContextMenu menu = new ContextMenu();

        MenuItem editarItem = new MenuItem("✏️ Editar");
        editarItem.setOnAction(e -> editarTarea(entry));

        MenuItem completarItem = new MenuItem("✅ Marcar como Completada");
        completarItem.setOnAction(e -> completarTarea(entry));

        MenuItem eliminarItem = new MenuItem("🗑️ Eliminar");
        eliminarItem.setOnAction(e -> eliminarTarea(entry));

        menu.getItems().addAll(editarItem, completarItem, eliminarItem);
        return menu;
    };

    // ==================== ACCIONES DE BOTONES ====================
    @FXML
    private void abrirFormularioTarea() {
        try {
            actualizarEstado("Abriendo formulario de nueva tarea...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CrearTarea.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Nueva Tarea");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            // Pasar referencia del controlador principal
            Object controller = loader.getController();
            if (controller instanceof CrearTareaController) {
                ((CrearTareaController) controller).setCalendarioController(this);
                ((CrearTareaController) controller).setUsuarioActual(usuarioActual);
            }

            stage.showAndWait();

            // Actualizar después de cerrar el formulario
            cargarTareasEnCalendario();
            actualizarEstadisticas();

        } catch (IOException e) {
            mostrarError("Error al abrir formulario", e.getMessage());
        }
    }

    @FXML
    private void abrirListaTareas() {
        try {
            actualizarEstado("Abriendo lista de tareas...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListaTareas.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Lista de Tareas");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

            // Actualizar después de cerrar
            cargarTareasEnCalendario();
            actualizarEstadisticas();

        } catch (IOException e) {
            mostrarError("Error al abrir lista de tareas", e.getMessage());
        }
    }

    @FXML
    private void abrirPerfil() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Perfil de Usuario");
        alert.setHeaderText("Información del Usuario");
        alert.setContentText(
                "Nombre: " + (usuarioActual != null ? usuarioActual.getNombre() : "No disponible") + "\n" +
                        "Email: " + (usuarioActual != null ? usuarioActual.getEmail() : "No disponible")
        );
        alert.showAndWait();
    }

    @FXML
    private void cambiarVistaCalendario() {
        String vista = cbVistaCalendario.getValue();
        if (vista != null && calendarView != null) {
            switch (vista) {
                case "Día":
                    calendarView.showDayPage();
                    break;
                case "Semana":
                    calendarView.showWeekPage();
                    break;
                case "Mes":
                    calendarView.showMonthPage();
                    break;
                case "Año":
                    calendarView.showYearPage();
                    break;
            }
            actualizarEstado("Vista cambiada a: " + vista);
        }
    }

    @FXML
    private void buscarTareas() {
        String termino = txtBuscar.getText().trim();
        if (termino.isEmpty()) {
            cargarTareasEnCalendario();
            return;
        }

        actualizarEstado("Buscando tareas con término: " + termino);

        if (usuarioActual != null) {
            List<Tarea> tareas = calendarioService.buscarTareas(usuarioActual.getEmail(), termino);
            actualizarCalendarioConTareas(tareas);
            actualizarEstado("Encontradas " + tareas.size() + " tareas");
        }
    }

    @FXML
    private void togglePanelLateral() {
        panelLateralVisible = !panelLateralVisible;
        panelLateral.setVisible(panelLateralVisible);
        panelLateral.setManaged(panelLateralVisible);

        btnTogglePanel.setText(panelLateralVisible ? "◀ Ocultar Panel" : "▶ Mostrar Panel");
    }

    // ==================== MÉTODOS AUXILIARES ====================
    public void cargarTareasEnCalendario() {
        if (usuarioActual == null) return;

        actualizarEstado("Cargando tareas...");
        mostrarPanelCarga(true);

        // Simular carga asíncrona
        Platform.runLater(() -> {
            try {
                List<Tarea> tareas = calendarioService.obtenerTareas(usuarioActual.getEmail());
                actualizarCalendarioConTareas(tareas);
                actualizarProximasTareas(tareas);
                actualizarEstado("Cargadas " + tareas.size() + " tareas");
            } catch (Exception e) {
                mostrarError("Error al cargar tareas", e.getMessage());
            } finally {
                mostrarPanelCarga(false);
            }
        });
    }

    private void actualizarCalendarioConTareas(List<Tarea> tareas) {
        calendarioTareas.clear();

        for (Tarea tarea : tareas) {
            Entry<String> entry = new Entry<>(tarea.getNombre());

            // Convertir fechas
            LocalDateTime inicio = tarea.getFechaInicio().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime fin = tarea.getFechaFin().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

            entry.setInterval(inicio, fin);
            entry.setLocation(tarea.getDescripcion());
            entry.setUserObject(tarea.getNombre());

            // Asignar color según estado
            switch (tarea.getEstado()) {
                case PENDIENTE:
                    entry.setCalendar(calendarioTareas);
                    break;
                case ENPROCESO:
                    entry.setCalendar(calendarioTareas);
                    // Cambiar estilo si es necesario
                    break;
                case COMPLETADA:
                    entry.setCalendar(calendarioTareas);
                    break;
            }

            calendarioTareas.addEntry(entry);
        }
    }

    private void actualizarEstadisticas() {
        if (usuarioActual == null) return;

        Platform.runLater(() -> {
            List<Tarea> tareas = calendarioService.obtenerTareas(usuarioActual.getEmail());

            int pendientes = 0, enProgreso = 0, completadas = 0, vencidas = 0;
            LocalDate hoy = LocalDate.now();

            for (Tarea tarea : tareas) {
                switch (tarea.getEstado()) {
                    case PENDIENTE:
                        pendientes++;
                        // Verificar si está vencida
                        LocalDate fechaFin = tarea.getFechaFin().toInstant()
                                .atZone(ZoneId.systemDefault()).toLocalDate();
                        if (fechaFin.isBefore(hoy)) {
                            vencidas++;
                        }
                        break;
                    case ENPROCESO:
                        enProgreso++;
                        break;
                    case COMPLETADA:
                        completadas++;
                        break;
                }
            }

            lblPendientes.setText(String.valueOf(pendientes));
            lblEnProgreso.setText(String.valueOf(enProgreso));
            lblCompletadas.setText(String.valueOf(completadas));
            lblVencidas.setText(String.valueOf(vencidas));
        });
    }

    private void actualizarProximasTareas(List<Tarea> tareas) {
        ObservableList<String> proximasTareas = FXCollections.observableArrayList();

        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        tareas.stream()
                .filter(t -> t.getEstado() != Estado.COMPLETADA)
                .sorted((t1, t2) -> t1.getFechaInicio().compareTo(t2.getFechaInicio()))
                .limit(10)
                .forEach(tarea -> {
                    LocalDate fecha = tarea.getFechaInicio().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    String fechaStr = fecha.format(formatter);
                    String prioridadIcon = tarea.getPrioridad() == Prioridad.INMEDIATO ? "🔴" :
                            tarea.getPrioridad() == Prioridad.IMPORTANTE ? "🟡" : "🟢";
                    proximasTareas.add(fechaStr + " " + prioridadIcon + " " + tarea.getNombre());
                });

        listaProximasTareas.setItems(proximasTareas);
    }

    // ==================== ACCIONES DE TAREAS ====================
    private void editarTarea(Entry<?> entry) {
        if (entry.getUserObject() instanceof Tarea) {
            Tarea tarea = (Tarea) entry.getUserObject();
            // Aquí abririías el formulario de edición
            actualizarEstado("Editando tarea: " + tarea.getNombre());
        }
    }

    private void completarTarea(Entry<?> entry) {
        if (entry.getUserObject() instanceof Tarea) {
            Tarea tarea = (Tarea) entry.getUserObject();
            boolean exito = calendarioService.cambiarEstadoTarea(tarea.getId(), Estado.COMPLETADA);

            if (exito) {
                cargarTareasEnCalendario();
                actualizarEstadisticas();
                actualizarEstado("Tarea completada: " + tarea.getNombre());
            } else {
                mostrarError("Error", "No se pudo completar la tarea");
            }
        }
    }

    private void eliminarTarea(Entry<?> entry) {
        if (entry.getUserObject() instanceof Tarea) {
            Tarea tarea = (Tarea) entry.getUserObject();

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Eliminación");
            confirmacion.setHeaderText("¿Eliminar tarea?");
            confirmacion.setContentText("¿Está seguro de que desea eliminar la tarea '" + tarea.getNombre() + "'?");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean exito = calendarioService.eliminarTarea(tarea.getId());

                if (exito) {
                    cargarTareasEnCalendario();
                    actualizarEstadisticas();
                    actualizarEstado("Tarea eliminada: " + tarea.getNombre());
                } else {
                    mostrarError("Error", "No se pudo eliminar la tarea");
                }
            }
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================
    private void simularUsuarioLogueado() {
        // Esto debería venir del login real
        usuarioActual = new Usuario("Usuario Demo", "demo@ejemplo.com", "123456");
        lblUsuario.setText("👤 Usuario: " + usuarioActual.getNombre());

        // Crear usuario si no existe
        if (!calendarioService.existeUsuario(usuarioActual.getEmail())) {
            calendarioService.crearUsuario(usuarioActual);
        }
    }

    private void actualizarFechaActual() {
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy");
        lblFechaActual.setText(hoy.format(formatter));
    }

    private void actualizarEstado(String mensaje) {
        Platform.runLater(() -> {
            lblEstado.setText(mensaje);
            System.out.println("ℹ️ " + mensaje);
        });
    }

    private void mostrarPanelCarga(boolean mostrar) {
        Platform.runLater(() -> {
            panelCarga.setVisible(mostrar);
        });
    }

    private void mostrarError(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    // ==================== GETTERS Y SETTERS ====================
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (usuario != null) {
            lblUsuario.setText("👤 Usuario: " + usuario.getNombre());
            cargarTareasEnCalendario();
            actualizarEstadisticas();
        }
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
