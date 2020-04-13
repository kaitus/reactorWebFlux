package com.springboot.reactor.apirest.apirest.models.repository;


import com.springboot.reactor.apirest.apirest.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaReposotiry extends ReactiveMongoRepository<Categoria, String> {

    public Mono<Categoria> findByNombre(String nombre);
}
