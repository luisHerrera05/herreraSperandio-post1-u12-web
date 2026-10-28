package com.ejemplo.miapp.controller;

import com.ejemplo.miapp.model.Producto;
import com.ejemplo.miapp.repository.ProductoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository repository;

    public ProductoController(ProductoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Producto> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@RequestBody Producto producto) {
        producto.setId(null);
        return repository.save(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto datos) {
        return repository.findById(id).map(existente -> {
            existente.setNombre(datos.getNombre());
            existente.setPrecio(datos.getPrecio());
            existente.setCategoria(datos.getCategoria());
            return ResponseEntity.ok(repository.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
