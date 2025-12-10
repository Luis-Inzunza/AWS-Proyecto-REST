package com.example.demo.repository;

import com.example.demo.model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Integer> {
    // Con esto nos permite hacer los .save(), .findById(), .delete(), etc. de los Profesores
}
