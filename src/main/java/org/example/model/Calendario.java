package org.example.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Calendario {
    private List<Tarea> tareas;

    public Calendario() {
        this.tareas = new ArrayList<>();
    }

    public List<Tarea> getTareas() {
        return tareas;
    }

    public void agregarTarea(Tarea tarea) {
        tareas.add(tarea);
    }

    public List<Tarea> buscarTarea(String nombre, int id) {
        List<Tarea> tareasEncontradas = new ArrayList<>();
        for (Tarea tarea : tareas) {
            if (tarea.getNombre().toLowerCase().contains(nombre.toLowerCase()) || tarea.getId() == id) {
                tareasEncontradas.add(tarea);
            }
        }
        return tareasEncontradas;
    }

    public boolean modificarTarea( int id, String nuevoNombre, String nuevaDescripcion) {
        for (Tarea tarea : tareas) {
            if ( tarea.getId() == id) {
                tarea.setNombre(nuevoNombre);
                tarea.setDescripcion(nuevaDescripcion);
                return true; //Tarea modificada exitosamente
            }
        }
        return false;//No se encontro la Tarea Buscada
    }

    public boolean eliminarTarea(int id) {
        for (Tarea tarea : tareas) {
            if (tarea.getId() == id) {
                tareas.remove(tarea);
                return true;// tarea eliminada exitosamente
            }
        }
        return false;//No se pudo eliminar tarea
    }


    public List<Tarea> obtenerTareasPorFecha(Date fecha) {
        List<Tarea> tareasEncontradas = new ArrayList<>();
        for (Tarea tarea : tareas) {
            if (tarea.getFechaFin().equals(fecha)) {
                tareasEncontradas.add(tarea);
            }
        }
        return tareasEncontradas;
    }

    public void mostrarTareas() {
        for (Tarea tarea : tareas) {
            System.out.println(tarea);
        }
    }
}
