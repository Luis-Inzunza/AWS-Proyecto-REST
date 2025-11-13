package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

import com.example.demo.model.Alumnos;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {
    private List<Alumnos> listaAlumnos = new ArrayList<>();

     @GetMapping
    public ResponseEntity<List<Alumnos>> obtenerAlumno(){
        return new ResponseEntity<>(listaAlumnos, HttpStatus.OK); // 200 OK
    }

    @PostMapping
    public ResponseEntity<Alumnos> crearAlumno(@RequestBody Alumnos alumno ){
        if (!validarAlumno(alumno)) {return new ResponseEntity<>(HttpStatus.BAD_REQUEST);} //400 BAD REQUEST
        
        listaAlumnos.add(alumno);

        return new ResponseEntity<>(alumno, HttpStatus.CREATED); //201 CREATED
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alumnos> alumnoId(@PathVariable int id) {
        for (Alumnos profesor : listaAlumnos) {
            if (profesor.getId() == id) {
                return new ResponseEntity<>(profesor, HttpStatus.OK); //200 OK
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); //404 NOT FOUND
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alumnos> actualizarAlumno(@PathVariable int id, @RequestBody Alumnos alumnoNuevo) {
        
        if (!validarAlumno(alumnoNuevo)) {return new ResponseEntity<>(HttpStatus.BAD_REQUEST);} //400 BAD REQUEST
        
        for (Alumnos alumno : listaAlumnos) {
            if (alumno.getId() == id) {
                alumno.setMatricula(alumnoNuevo.getMatricula());
                alumno.setNombres(alumnoNuevo.getNombres());
                alumno.setApellidos(alumnoNuevo.getApellidos());
                alumno.setPromedio(alumnoNuevo.getPromedio());
                
                return new ResponseEntity<>(alumnoNuevo, HttpStatus.OK); //200 OK  
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); //404 NOT FOUND
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Alumnos> eliminarProfesor(@PathVariable int id) {
        for (Alumnos alumno : listaAlumnos) {
            if (alumno.getId() == id) {
                listaAlumnos.remove(alumno);
                return new ResponseEntity<>(alumno, HttpStatus.OK); //200 OK
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); //404 NOT FOUND
    }

    private boolean validarAlumno(Alumnos alumno) {
        if (alumno.getNombres() == null || alumno.getNombres().isEmpty()) {
            return false;
        }
        if (alumno.getApellidos() == null || alumno.getApellidos().isEmpty()) {
            return false;
        }
        if (alumno.getMatricula() == null || alumno.getMatricula().isEmpty()) {
            return false;
        }
        if (alumno.getPromedio() <= 0) return false;
        
        return true;
    }
}
