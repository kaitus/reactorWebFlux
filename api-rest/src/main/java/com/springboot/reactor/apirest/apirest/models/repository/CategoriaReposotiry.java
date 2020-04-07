package com.springboot.reactor.apirest.apirest.models.repository;


import com.springboot.reactor.apirest.apirest.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaReposotiry extends ReactiveMongoRepository<Categoria, String> {
}
