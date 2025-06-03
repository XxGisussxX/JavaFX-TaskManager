package org.example.model;

import java.util.List;

public class Usuario {
    private int id;
    private String nombre;
    private String email;
    private String contrasena;
    private List<Calendario> calendarios;

    public Usuario(String nombre, String email, String contrasena) {
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;

    }
    public Calendario obtenerCalendario(){

        return calendarios.get(0);
    }
    public boolean autenticar(String contrasena){

        return this.contrasena.equals(contrasena);
    }
}
