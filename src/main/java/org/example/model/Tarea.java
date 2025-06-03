package org.example.model;

import java.util.Date;

public class Tarea {
    private static  int contadorId=1;
    private int id;
    private String nombre;
    private Date fechaInicio;
    private Date fechaFin;
    private Prioridad prioridad;
    private Estado estado;
    private Date recordatorio;

    public Tarea(String nombre, Date fechaInicio, Date fechaFin, Prioridad prioridad, Date recordatorio) {
        this.id = contadorId++;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.prioridad = prioridad;
        this.estado = Estado.PENDIENTE;
        this.recordatorio = recordatorio;
    }

    public void marcarComoHecho(){
        this.estado = Estado.COMPLETADA;
    }

    public String getNombre() {
        return nombre;
    }

    public int getId() {
        return id;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }
    public Date getFechaFin() {
        return fechaFin;
    }
    public Prioridad getPrioridad() {
        return prioridad;
    }
    public Estado getEstado() {
        return estado;
    }

    public String setNombre(String nombre) {

        return nombre = nombre;
    }

    public String setDescripcion(String descripcion) {

        return descripcion;
    }

    @Override
    public String toString() {
        return "Nombre: " + nombre + " ID: " + id;
    }
}
