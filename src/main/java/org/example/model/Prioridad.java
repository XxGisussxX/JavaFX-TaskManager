package org.example.model;

public enum Prioridad {
    INMEDIATO("Inmediato"),
    IMPORTANTE("Importante"),
    CONTIEMPO("Contiempo");

    private final String descripcion;

     Prioridad(String descripcion) {
        this.descripcion = descripcion;
    }
}
