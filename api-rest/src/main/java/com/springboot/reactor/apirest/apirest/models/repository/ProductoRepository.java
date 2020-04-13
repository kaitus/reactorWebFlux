package com.springboot.reactor.apirest.apirest.models.repository;

import com.springboot.reactor.apirest.apirest.models.documents.Producto;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductoRepository extends ReactiveMongoRepository<Producto, String> {

    public Mono<Producto> findByNombre(String nombre);

    @Query("{ 'nombre':?0} ")
    public Mono<Producto> obtenerPorNombre(String nombre);

}
