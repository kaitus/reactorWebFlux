package com.springboot.webflux.app.controllers;

import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/producto")
public class ProductoRestController {

    @Autowired
    private ProductoRepository repository;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping
    public Flux<Producto> index() {
        Flux<Producto> productos = repository.findAll().map(producto -> {
            producto.setNombre(producto.getNombre().toUpperCase());
            return producto;
        })
        .doOnNext(prod -> log.info(prod.getNombre()));

        return productos;
    }

    @GetMapping("/{id}")
    public Mono<Producto> show(@PathVariable String id) {
        Flux<Producto> productos = repository.findAll();
        Mono<Producto> producto = productos.filter(p -> p.getId().equals(id))
                .next()
                .doOnNext(prod -> log.info(prod.getNombre()));

        return repository.findById(id);
    }
}
