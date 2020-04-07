package com.springboot.reactor.apirest.apirest;

import com.springboot.reactor.apirest.apirest.models.documents.Categoria;
import com.springboot.reactor.apirest.apirest.models.documents.Producto;
import com.springboot.reactor.apirest.apirest.models.services.ProductoServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class ApiRestApplication implements CommandLineRunner {

	@Autowired
	private ProductoServices service;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(ApiRestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ApiRestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();

		Categoria electronico = new Categoria("Electronico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computacion");
		Categoria muebles = new Categoria("Muebles");

		Flux.just(electronico, deporte, computacion,muebles)
				.flatMap(service::saveCategoria).doOnNext(c -> {
			log.info("Categoria Creada" + c.getNombre() + " Id: " + c.getId());
		}).thenMany(Flux.just(new Producto("TV Samsung", 200000.00, electronico),
				new Producto("TV Sonic", 300000.00, electronico),
				new Producto("TV Apple", 400000.00, electronico),
				new Producto("TV Smart", 450000.00, electronico),
				new Producto("Bicicleta", 500000.00, deporte),
				new Producto("Mueble", 600000.00, muebles),
				new Producto("TV KLE", 700000.00, electronico),
				new Producto("HP LAPTOP", 800000.00, computacion),
				new Producto("APPLE LAPTOP", 900000.00, computacion))
				.flatMap(producto -> {
					producto.setCreateAt(new Date());
					return service.save(producto);
				}))
				.subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
	}
}
