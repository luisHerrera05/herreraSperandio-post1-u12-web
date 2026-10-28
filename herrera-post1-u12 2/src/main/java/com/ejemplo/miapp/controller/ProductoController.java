package com.ejemplo.miapp.controller;

import com.ejemplo.miapp.model.Categoria;
import com.ejemplo.miapp.model.Producto;
import com.ejemplo.miapp.repository.CategoriaRepository;
import com.ejemplo.miapp.repository.ProductoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public ProductoController(ProductoRepository productos, CategoriaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    @GetMapping
    public List<Producto> listar() {
        return productos.findAll();
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@Valid @RequestBody Producto producto) {
        producto.setId(null);
        producto.setCategoria(resolverCategoria(producto.getCategoria()));
        return productos.save(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody Producto datos) {
        Producto existente = obtener(id);
        existente.setNombre(datos.getNombre());
        existente.setPrecio(datos.getPrecio());
        existente.setCategoria(resolverCategoria(datos.getCategoria()));
        return productos.save(existente);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        obtener(id);
        productos.deleteById(id);
    }

    private Categoria resolverCategoria(Categoria referencia) {
        if (referencia == null || referencia.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría (con id) es obligatoria");
        }
        return categorias.findById(referencia.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría indicada no existe"));
    }
}
