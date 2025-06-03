package org.example.controller;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.MainApp;
import org.example.model.Calendario;
import org.example.model.Estado;
import org.example.model.Prioridad;
import org.example.model.Tarea;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarController {
    private Calendario calendario;
    private ObservableList<Tarea> tareasObservable;

    // Calendarios de CalendarFX
    private Calendar calendarInmediato;
    private Calendar calendarImportante;
    private Calendar calendarConTiempo;

    // Mapeo de tareas a entradas de CalendarFX
    private Map<Integer, Entry<?>> entryMap;

    public CalendarController() {
        this.calendario = new Calendario();
        this.tareasObservable = FXCollections.observableArrayList();
        this.entryMap = new HashMap<>();
    }

    public void setCalendarios(Calendar calendarInmediato, Calendar calendarImportante, Calendar calendarConTiempo) {
        this.calendarInmediato = calendarInmediato;
        this.calendarImportante = calendarImportante;
        this.calendarConTiempo = calendarConTiempo;
    }

    public ObservableList<Tarea> getTareasObservable() {
        return tareasObservable;
    }

    public void agregarTarea(String nombre, Date fechaInicio, Date fechaFin, Prioridad prioridad, Date recordatorio) {
        Tarea nuevaTarea = new Tarea(nombre, fechaInicio, fechaFin, prioridad, recordatorio);

        // Agregar tarea al modelo
        calendario.agregarTarea(nuevaTarea);

        // Actualizar la lista observable para la UI
        tareasObservable.add(nuevaTarea);

        // Crear entrada en el calendario
        crearEntradaCalendario(nuevaTarea);
    }

    private void crearEntradaCalendario(Tarea tarea) {
        // Crear una nueva entrada para el calendario
        Entry<Tarea> entry = new Entry<>(tarea.getNombre());

        // Configurar la entrada con los datos de la tarea
        LocalDateTime inicio = MainApp.convertToLocalDateTime(tarea.getFechaInicio());
        LocalDateTime fin = MainApp.convertToLocalDateTime(tarea.getFechaFin());

        entry.setInterval(new Interval(inicio, fin));
        entry.setUserObject(tarea);

        // Aplicar estilos según la prioridad
        setEntryStyle(entry, tarea.getId(), tarea.getPrioridad());

        // Agregar la entrada al calendario correspondiente según prioridad
        Calendar targetCalendar = getCalendarForPriority(tarea.getPrioridad());
        targetCalendar.addEntry(entry);

        // Mapear la entrada con el ID de la tarea
        entryMap.put(tarea.getId(), entry);
    }

    private void setEntryStyle(Entry<?> entry, int tareaId, Prioridad prioridad) {
        // Configurar estilo según prioridad
        switch (prioridad) {
            case INMEDIATO:
                entry.setTitle("[INMEDIATO] " + entry.getTitle());
                break;
            case IMPORTANTE:
                entry.setTitle("[IMPORTANTE] " + entry.getTitle());
                break;
            case CONTIEMPO:
                entry.setTitle("[NORMAL] " + entry.getTitle());
                break;
        }

        // Guardar el ID de la tarea como propiedad
        entry.getProperties().put("tareaId", tareaId);
    }

    private Calendar getCalendarForPriority(Prioridad prioridad) {
        switch (prioridad) {
            case INMEDIATO:
                return calendarInmediato;
            case IMPORTANTE:
                return calendarImportante;
            case CONTIEMPO:
            default:
                return calendarConTiempo;
        }
    }

    public void eliminarTarea(int id) {
        // Eliminar del modelo
        boolean eliminado = calendario.eliminarTarea(id);

        if (eliminado) {
            // Eliminar de la lista observable
            tareasObservable.removeIf(tarea -> tarea.getId() == id);

            // Eliminar del calendario
            eliminarEntradaCalendario(id);
        }
    }

    private void eliminarEntradaCalendario(int tareaId) {
        Entry<?> entry = entryMap.get(tareaId);
        if (entry != null) {
            // Obtener el calendario al que pertenece la entrada
            Calendar calendar = entry.getCalendar();
            if (calendar != null) {
                calendar.removeEntry(entry);
            }

            // Eliminar del mapa
            entryMap.remove(tareaId);
        }
    }

    public void modificarTarea(int id, String nuevoNombre, String nuevaDescripcion) {
        boolean modificado = calendario.modificarTarea(id, nuevoNombre, nuevaDescripcion);

        if (modificado) {
            // Actualizar la lista observable
            actualizarListaObservable();

            // Actualizar la entrada en el calendario
            Entry<?> entry = entryMap.get(id);
            if (entry != null) {
                entry.setTitle(nuevoNombre);
            }
        }
    }

    public void actualizarEstadoTarea(int id, Estado nuevoEstado) {
        // Actualizar visualmente la entrada en el calendario según el estado
        Entry<?> entry = entryMap.get(id);
        if (entry != null) {
            if (nuevoEstado == Estado.COMPLETADA) {
                entry.setTitle("[COMPLETADA] " + entry.getTitle().replaceAll("\\[.*?\\] ", ""));
            }
        }

        // Actualizar la lista observable
        actualizarListaObservable();
    }

    public void buscarTareas(String termino) {
        // Buscar tareas por nombre (id 0 para ignorar la búsqueda por ID)
        List<Tarea> tareasEncontradas = calendario.buscarTarea(termino, 0);

        // Actualizar lista observable con los resultados
        tareasObservable.clear();
        tareasObservable.addAll(tareasEncontradas);
    }

    public void mostrarTodasLasTareas() {
        // Obtener todas las tareas del calendario
        List<Tarea> todasLasTareas = calendario.getTareas();

        // Actualizar lista observable
        tareasObservable.clear();
        tareasObservable.addAll(todasLasTareas);
    }

    public void obtenerTareasPorFecha(Date fecha) {
        List<Tarea> tareasFecha = calendario.obtenerTareasPorFecha(fecha);

        // Actualizar lista observable
        tareasObservable.clear();
        tareasObservable.addAll(tareasFecha);
    }

    private void actualizarListaObservable() {
        // Refrescar completamente la lista
        List<Tarea> todasLasTareas = calendario.getTareas();

        tareasObservable.clear();
        tareasObservable.addAll(todasLasTareas);
    }

    public void seleccionarTareaEnCalendario(int id) {
        Entry<?> entry = entryMap.get(id);
        if (entry != null) {
            // Establecer esta entrada como seleccionada
           // entry.setSelected(true);
        }
    }

    public void cargarTareasExistentes() {
        // Si ya existen tareas en el modelo, cargarlas en el calendario visual
        for (Tarea tarea : calendario.getTareas()) {
            crearEntradaCalendario(tarea);
        }
    }
}