package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alumnos")
public class Alumnos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 2. El ID lo genera la BD automáticamente
    private int id;
    
    private String nombres;
    private String apellidos;
    private String matricula;
    private float promedio;

    private String fotoPerfilUrl;
    private String password;

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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
    public String getMatricula() {
        return matricula;
    }
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
    public float getPromedio() {
        return promedio;
    }
    public void setPromedio(float promedio) {
        this.promedio = promedio;
    }

    public String getFotoPerfilUrl() { return fotoPerfilUrl; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

}
