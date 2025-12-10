package com.example.demo.controller;

import com.example.demo.model.Profesor;
import com.example.demo.repository.ProfesorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/profesores")
public class ProfesorController {

    @Autowired
    private ProfesorRepository profesorRepository;

    @GetMapping
    public List<Profesor> obtenerProfesores() {
        return profesorRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profesor> obtenerProfesorPorId(@PathVariable int id) {
        Optional<Profesor> profesor = profesorRepository.findById(id);
        return profesor.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Profesor> crearProfesor(@RequestBody Profesor profesor) {
        if (!validarProfesor(profesor)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Profesor profesorGuardado = profesorRepository.save(profesor);
        return new ResponseEntity<>(profesorGuardado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profesor> actualizarProfesor(@PathVariable int id, @RequestBody Profesor profesorNuevo) {
        if (!validarProfesor(profesorNuevo)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return profesorRepository.findById(id)
                .map(profesor -> {
                    profesor.setNombres(profesorNuevo.getNombres());
                    profesor.setApellidos(profesorNuevo.getApellidos());
                    profesor.setNumeroEmpleado(profesorNuevo.getNumeroEmpleado());
                    profesor.setHorasClase(profesorNuevo.getHorasClase());
                    profesorRepository.save(profesor);
                    return new ResponseEntity<>(profesor, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminarProfesor(@PathVariable int id) {
        if (profesorRepository.existsById(id)) {
            profesorRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private boolean validarProfesor(Profesor profesor) {
        if (profesor.getNombres() == null || profesor.getNombres().isEmpty()) return false;
        if (profesor.getApellidos() == null || profesor.getApellidos().isEmpty()) return false;
        if (profesor.getNumeroEmpleado() <= 0) return false;
        if (profesor.getHorasClase() <= 0) return false;
        return true;
    }
}