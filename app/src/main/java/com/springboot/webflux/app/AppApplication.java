package com.springboot.webflux.app;

import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.repository.ProductoRepository;
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
public class AppApplication implements CommandLineRunner {

	@Autowired
	private ProductoRepository repository;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(AppApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("productos")
				.subscribe();

		Flux.just(new Producto("TV Samsung", 200000.00),
				new Producto("TV Sonic", 300000.00),
				new Producto("TV Apple", 400000.00),
				new Producto("TV Smart", 450000.00),
				new Producto("TV Dell", 500000.00),
				new Producto("TV Lenovo", 600000.00),
				new Producto("TV KLE", 700000.00),
				new Producto("HP LAPTOP", 800000.00),
				new Producto("APPLE LAPTOP", 900000.00))
				.flatMap(producto -> {
					producto.setCreateAt(new Date());
					return repository.save(producto);
				})
				.subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
	}
}
