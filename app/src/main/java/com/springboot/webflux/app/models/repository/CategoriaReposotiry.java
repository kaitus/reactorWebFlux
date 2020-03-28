package com.springboot.webflux.app.models.repository;


import com.springboot.webflux.app.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaReposotiry extends ReactiveMongoRepository<Categoria, String> {
}
