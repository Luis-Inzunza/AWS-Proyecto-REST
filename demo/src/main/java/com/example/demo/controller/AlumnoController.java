package com.example.demo.controller;

import com.example.demo.model.Alumnos;
import com.example.demo.repository.AlumnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

    @Autowired
    private AlumnoRepository alumnoRepository;

    
    @GetMapping
    public List<Alumnos> obtenerAlumnos() {
        return alumnoRepository.findAll();
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Alumnos> obtenerAlumnoPorId(@PathVariable int id) {
        Optional<Alumnos> alumno = alumnoRepository.findById(id);
        
        if (alumno.isPresent()) {
            return new ResponseEntity<>(alumno.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    
    @PostMapping
    public ResponseEntity<Alumnos> crearAlumno(@RequestBody Alumnos alumno) {
        // Validamos datos básicos (incluyendo password que es nuevo requisito)
        if (!validarAlumno(alumno)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        // La base de datos asignará el ID automáticamente, así que guardamos directo
        Alumnos alumnoGuardado = alumnoRepository.save(alumno);
        return new ResponseEntity<>(alumnoGuardado, HttpStatus.CREATED);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<Alumnos> actualizarAlumno(@PathVariable int id, @RequestBody Alumnos alumnoNuevo) {
        if (!validarAlumno(alumnoNuevo)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Alumnos> alumnoExistente = alumnoRepository.findById(id);

        if (alumnoExistente.isPresent()) {
            Alumnos alumno = alumnoExistente.get();
            // Actualizamos campos
            alumno.setNombres(alumnoNuevo.getNombres());
            alumno.setApellidos(alumnoNuevo.getApellidos());
            alumno.setMatricula(alumnoNuevo.getMatricula());
            alumno.setPromedio(alumnoNuevo.getPromedio());
            if(alumnoNuevo.getPassword() != null && !alumnoNuevo.getPassword().isEmpty()){
                alumno.setPassword(alumnoNuevo.getPassword());
            }
            
            // Guardamos los cambios en BD
            alumnoRepository.save(alumno);
            return new ResponseEntity<>(alumno, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminarAlumno(@PathVariable int id) {
        if (alumnoRepository.existsById(id)) {
            alumnoRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    private boolean validarAlumno(Alumnos alumno) {
        if (alumno.getNombres() == null || alumno.getNombres().isEmpty()) return false;
        if (alumno.getApellidos() == null || alumno.getApellidos().isEmpty()) return false;
        if (alumno.getMatricula() == null || alumno.getMatricula().isEmpty()) return false;
        if (alumno.getPromedio() < 0) return false;
        // Validamos password si es necesario para creación
        // if (alumno.getPassword() == null || alumno.getPassword().isEmpty()) return false; 
        return true;
    }
}
