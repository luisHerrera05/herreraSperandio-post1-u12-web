package com.ejemplo.miapp.repository;

import com.ejemplo.miapp.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
