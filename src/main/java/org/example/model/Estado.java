package org.example.model;

public enum Estado {
    PENDIENTE("Tarea pendiete"),
    ENPROCESO("Tarea en proceso"),
    COMPLETADA("Tarea completada");

    private final String descripcion;

    Estado(String descripcion) {
        this.descripcion = descripcion;
    }
}
