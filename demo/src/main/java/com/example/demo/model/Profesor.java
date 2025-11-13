package com.example.demo.model;

public class Profesor {
    private int id;
    private int numeroEmpleado;
    private String nombres;
    private String apellidos;
    private int horasClase;

    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getNumeroEmpleado() {
        return numeroEmpleado;
    }
    public void setNumeroEmpleado(int numeroEmpleado) {
        this.numeroEmpleado = numeroEmpleado;
    }
    public String getNombres() {
        return nombres;
    }
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }
    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    public int getHorasClase() {
        return horasClase;
    }
    public void setHorasClase(int horasClase) {
        this.horasClase = horasClase;
    }

}
