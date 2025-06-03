package org.example;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.controller.CalendarController;
import org.example.model.Estado;
import org.example.model.Prioridad;
import org.example.model.Tarea;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class MainApp extends Application {
    private CalendarController controller;
    private ObservableList<Tarea> tareasList;
    private ListView<Tarea> listView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private CalendarView calendarView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new CalendarController();
        tareasList = controller.getTareasObservable();

        // Configurar el calendario
        configurarCalendario();

        BorderPane root = new BorderPane();

        // Crear panel de formulario
        GridPane formPane = createFormPane();

        // Crear lista de tareas
        listView = new ListView<>(tareasList);
        listView.setCellFactory(param -> new ListCell<Tarea>() {
            @Override
            protected void updateItem(Tarea tarea, boolean empty) {
                super.updateItem(tarea, empty);
                if (empty || tarea == null) {
                    setText(null);
                } else {
                    setText(tarea.toString());
                }
            }
        });

        // Manejar selección en la lista
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Seleccionar la entrada correspondiente en el calendario
                controller.seleccionarTareaEnCalendario(newSelection.getId());

                // Centrar la vista del calendario en la fecha de esta tarea
                if (newSelection.getFechaFin() != null) {
                    LocalDate fecha = convertToLocalDate(newSelection.getFechaFin());
                    calendarView.setDate(fecha);
                }
            }
        });

        // Crear panel de búsqueda y acciones
        VBox actionsPane = createActionsPane();

        // Configurar el layout
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.getChildren().addAll(formPane, actionsPane, listView);
        leftPanel.setPrefWidth(300);

        root.setLeft(leftPanel);
        root.setCenter(calendarView);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Gestión de Tareas con CalendarFX");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Thread para actualizar la hora del calendario
        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        calendarView.setToday(LocalDate.now());
                        calendarView.setTime(LocalTime.now());
                    });

                    try {
                        // Actualizar cada 10 segundos
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();
    }

    private void configurarCalendario() {
        calendarView = new CalendarView();

        // Crear calendarios por prioridad
        Calendar calendarInmediato = new Calendar("Inmediato");
        calendarInmediato.setStyle(Style.STYLE1);

        Calendar calendarImportante = new Calendar("Importante");
        calendarImportante.setStyle(Style.STYLE2);

        Calendar calendarConTiempo = new Calendar("Con Tiempo");
        calendarConTiempo.setStyle(Style.STYLE3);

        // Agregar calendarios a una fuente
        CalendarSource myCalendarSource = new CalendarSource("Mis Tareas");
        myCalendarSource.getCalendars().addAll(calendarInmediato, calendarImportante, calendarConTiempo);

        // Registrar la fuente en la vista
        calendarView.getCalendarSources().add(myCalendarSource);

        // Pasarle los calendarios al controlador
        controller.setCalendarios(calendarInmediato, calendarImportante, calendarConTiempo);

        // Configurar la vista
        calendarView.setRequestedTime(LocalTime.now());

        // Eliminar algunas páginas que no necesitamos
        calendarView.getDayPage().setDayPageLayout(calendarView.getDayPage().getDayPageLayout());

        // Solo mostrar la zona horaria por defecto
        calendarView.setShowDeveloperConsole(false);
    }

    private GridPane createFormPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        // Campos del formulario
        Label lblTitulo = new Label("Nueva Tarea");
        Label lblNombre = new Label("Nombre:");
        TextField txtNombre = new TextField();

        Label lblFechaInicio = new Label("Fecha Inicio (dd/MM/yyyy HH:mm):");
        TextField txtFechaInicio = new TextField();

        Label lblFechaFin = new Label("Fecha Fin (dd/MM/yyyy HH:mm):");
        TextField txtFechaFin = new TextField();

        Label lblPrioridad = new Label("Prioridad:");
        ComboBox<Prioridad> comboPrioridad = new ComboBox<>();
        comboPrioridad.getItems().addAll(Prioridad.INMEDIATO, Prioridad.IMPORTANTE, Prioridad.CONTIEMPO);
        comboPrioridad.setValue(Prioridad.CONTIEMPO);

        Label lblRecordatorio = new Label("Recordatorio (dd/MM/yyyy HH:mm):");
        TextField txtRecordatorio = new TextField();

        Button btnAgregar = new Button("Agregar Tarea");
        btnAgregar.setOnAction(e -> {
            try {
                String nombre = txtNombre.getText();
                Date fechaInicio = dateFormat.parse(txtFechaInicio.getText());
                Date fechaFin = dateFormat.parse(txtFechaFin.getText());
                Prioridad prioridad = comboPrioridad.getValue();
                Date recordatorio = dateFormat.parse(txtRecordatorio.getText());

                controller.agregarTarea(nombre, fechaInicio, fechaFin, prioridad, recordatorio);

                // Limpiar formulario
                txtNombre.clear();
                txtFechaInicio.clear();
                txtFechaFin.clear();
                comboPrioridad.setValue(Prioridad.CONTIEMPO);
                txtRecordatorio.clear();

            } catch (ParseException ex) {
                mostrarAlerta("Error de formato", "Por favor, introduce las fechas en formato dd/MM/yyyy HH:mm");
            }
        });

        // Agregar elementos al grid
        grid.add(lblTitulo, 0, 0, 2, 1);
        grid.add(lblNombre, 0, 1);
        grid.add(txtNombre, 1, 1);
        grid.add(lblFechaInicio, 0, 2);
        grid.add(txtFechaInicio, 1, 2);
        grid.add(lblFechaFin, 0, 3);
        grid.add(txtFechaFin, 1, 3);
        grid.add(lblPrioridad, 0, 4);
        grid.add(comboPrioridad, 1, 4);
        grid.add(lblRecordatorio, 0, 5);
        grid.add(txtRecordatorio, 1, 5);
        grid.add(btnAgregar, 1, 6);

        return grid;
    }

    private VBox createActionsPane() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));

        TextField txtBusqueda = new TextField();
        txtBusqueda.setPromptText("Buscar por nombre");

        Button btnBuscar = new Button("Buscar");
        btnBuscar.setOnAction(e -> {
            String termino = txtBusqueda.getText();
            controller.buscarTareas(termino);
        });

        Button btnMostrarTodas = new Button("Mostrar Todas");
        btnMostrarTodas.setOnAction(e -> {
            controller.mostrarTodasLasTareas();
        });

        Button btnEliminar = new Button("Eliminar Seleccionada");
        btnEliminar.setOnAction(e -> {
            Tarea seleccionada = listView.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                controller.eliminarTarea(seleccionada.getId());
            } else {
                mostrarAlerta("Error", "Por favor, selecciona una tarea para eliminar");
            }
        });

        Button btnCompletarTarea = new Button("Completar Tarea");
        btnCompletarTarea.setOnAction(e -> {
            Tarea seleccionada = listView.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                seleccionada.marcarComoHecho();
                controller.actualizarEstadoTarea(seleccionada.getId(), Estado.COMPLETADA);
            } else {
                mostrarAlerta("Error", "Por favor, selecciona una tarea para completar");
            }
        });

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                new HBox(10, txtBusqueda, btnBuscar),
                new HBox(10, btnMostrarTodas, btnEliminar, btnCompletarTarea)
        );

        return vbox;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Métodos de conversión para fechas
    public static LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDate convertToLocalDate(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static Date convertToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}