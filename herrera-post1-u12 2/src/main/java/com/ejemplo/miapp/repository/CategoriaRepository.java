package com.ejemplo.miapp.repository;

import com.ejemplo.miapp.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
