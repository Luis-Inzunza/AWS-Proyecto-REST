package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

import com.example.demo.model.Profesor;

@RestController
@RequestMapping("/profesores")
public class ProfesorController {
    private List<Profesor> listaProfesores = new ArrayList<>();

    @GetMapping
    public ResponseEntity<List<Profesor>> obtenerProfesor(){
        return new ResponseEntity<>(listaProfesores, HttpStatus.OK); // 200 OK
    }

    @PostMapping
    public ResponseEntity<Profesor> crearProfesor(@RequestBody Profesor profesor ){
        if (!validarProfesor(profesor)) {return new ResponseEntity<>(HttpStatus.BAD_REQUEST);} //400 BAD REQUEST
        
        listaProfesores.add(profesor);

        return new ResponseEntity<>(profesor, HttpStatus.CREATED); //201 CREATED
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profesor> profesorId(@PathVariable int id) {
        for (Profesor profesor : listaProfesores) {
            if (profesor.getId() == id) {
                return new ResponseEntity<>(profesor, HttpStatus.OK); //200 OK
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); //404 NOT FOUND
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profesor> actualizarProfesor(@PathVariable int id, @RequestBody Profesor profesorNuevo) {
        
        if (!validarProfesor(profesorNuevo)) {return new ResponseEntity<>(HttpStatus.BAD_REQUEST);} //400 BAD REQUEST
        
        for (Profesor profesor : listaProfesores) {
            if (profesor.getId() == id) {
                profesor.setNumeroEmpleado(profesorNuevo.getNumeroEmpleado());
                profesor.setNombres(profesorNuevo.getNombres());
                profesor.setApellidos(profesorNuevo.getApellidos());
                profesor.setHorasClase(profesorNuevo.getHorasClase());
                
                return new ResponseEntity<>(profesor, HttpStatus.OK); //200 OK  
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); //404 NOT FOUND
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Profesor> eliminarProfesor(@PathVariable int id) {
        for (Profesor profesor : listaProfesores) {
            if (profesor.getId() == id) {
                listaProfesores.remove(profesor);
                return new ResponseEntity<>(profesor, HttpStatus.OK); //200 OK
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); //404 NOT FOUND
    }

    private boolean validarProfesor(Profesor profesor) {
        if (profesor.getNombres() == null || profesor.getNombres().isEmpty()) {
            return false;
        }
        if (profesor.getApellidos() == null || profesor.getApellidos().isEmpty()) {
            return false;
        }
        if (profesor.getNumeroEmpleado() <= 0) {
            return false;
        }
        if (profesor.getHorasClase() <= 0) {
            return false;
        }
        
        return true;
    }

}
