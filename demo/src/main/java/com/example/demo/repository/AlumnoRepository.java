package com.example.demo.repository;

import com.example.demo.model.Alumnos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumnos, Integer> {
    // Con esto nos permite hacer los .save(), .findById(), .delete(), etc. de los Alumnos
}