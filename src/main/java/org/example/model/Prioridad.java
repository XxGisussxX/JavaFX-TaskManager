package org.example.model;

public enum Prioridad {
    INMEDIATO("Inmediato"),
    IMPORTANTE("Importante"),
    CONTIEMPO("Con tiempo");

    private final String descripcion;

    Prioridad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
