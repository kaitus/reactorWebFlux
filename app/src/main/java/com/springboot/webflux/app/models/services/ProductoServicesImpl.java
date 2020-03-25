package com.springboot.webflux.app.models.services;

import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServicesImpl implements ProductoServices {

    @Autowired
    private ProductoRepository repository;

    @Override
    public Flux<Producto> findAll() {
        return repository.findAll();
    }

    @Override
    public Flux<Producto> findAllConNombreUpperCase() {
        return repository.findAll().map(producto -> {
            producto.setNombre(producto.getNombre().toUpperCase());
            return producto;
        });
    }

    @Override
    public Flux<Producto> findAllConNombreUpperCaseRepeat() {
        return findAllConNombreUpperCase().repeat(5000);
    }

    @Override
    public Mono<Producto> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return repository.save(producto);
    }

    @Override
    public Mono<Void> delete(Producto producto) {
        return repository.delete(producto);
    }
}
