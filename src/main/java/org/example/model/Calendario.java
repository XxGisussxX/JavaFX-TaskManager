package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Calendario {
    private List<Tarea> tareas;
    private String archivoTareas; // Archivo donde se guardan las tareas


    public Calendario(String archivoTareas) {
        this.tareas = new ArrayList<>();
        this.archivoTareas = archivoTareas;
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

    public boolean modificarTarea(int id, String nuevoNombre, String nuevaDescripcion) {
        for (Tarea tarea : tareas) {
            if (tarea.getId() == id) {
                tarea.setNombre(nuevoNombre);
                tarea.setDescripcion(nuevaDescripcion);
                 // Guardar cambios
                System.out.println("💾 Cambios guardados automáticamente.");
                return true; // Tarea modificada exitosamente
            }
        }
        return false; // No se encontró la tarea buscada
    }

    public boolean eliminarTarea(int id) {
        for (int i = 0; i < tareas.size(); i++) {
            if (tareas.get(i).getId() == id) {
                tareas.remove(i);
                // Guardar cambios
                System.out.println("💾 Eliminación guardada automáticamente.");
                return true; // Tarea eliminada exitosamente
            }
        }
        return false; // No se pudo eliminar tarea
    }

    public List<Tarea> obtenerTareasPorEstado(Estado estado) {
        List<Tarea> tareasEncontradas = new ArrayList<>();
        for (Tarea tarea : tareas) {
            if (tarea.getEstado() == estado) {
                tareasEncontradas.add(tarea);
            }
        }
        return tareasEncontradas;
    }

    public List<Tarea> obtenerTareasPorPrioridad(Prioridad prioridad) {
        List<Tarea> tareasEncontradas = new ArrayList<>();
        for (Tarea tarea : tareas) {
            if (tarea.getPrioridad() == prioridad) {
                tareasEncontradas.add(tarea);
            }
        }
        return tareasEncontradas;
    }

    public boolean marcarTareaComoCompletada(int id) {
        for (Tarea tarea : tareas) {
            if (tarea.getId() == id) {
                tarea.marcarComoHecho();
                 // Guardar cambios
                System.out.println("💾 Estado actualizado y guardado automáticamente.");
                return true;
            }
        }
        return false;
    }

    public boolean marcarTareaEnProceso(int id) {
        for (Tarea tarea : tareas) {
            if (tarea.getId() == id) {
                tarea.marcarEnProceso();
                // Guardar cambios
                System.out.println("💾 Estado actualizado y guardado automáticamente.");
                return true;
            }
        }
        return false;
    }

    public void mostrarTareas() {
        if (tareas.isEmpty()) {
            System.out.println("📝 No hay tareas registradas.");
            return;
        }

        System.out.println("\n📋 === LISTADO DE TAREAS ===");
        System.out.println("📄 Archivo: " + (archivoTareas != null ? archivoTareas : "Sin archivo"));
        for (Tarea tarea : tareas) {
            System.out.println(tarea);
            System.out.println("─".repeat(50));
        }
    }

    public int contarTareas() {
        return tareas.size();
    }

    public int contarTareasPorEstado(Estado estado) {
        int contador = 0;
        for (Tarea tarea : tareas) {
            if (tarea.getEstado() == estado) {
                contador++;
            }
        }
        return contador;
    }
}
