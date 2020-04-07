package com.springboot.reactor.apirest.apirest.models.repository;

import com.springboot.reactor.apirest.apirest.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoRepository extends ReactiveMongoRepository<Producto, String> {

}
