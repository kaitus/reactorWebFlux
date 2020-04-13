package com.springboot.reactor.apirest.apirest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.reactor.apirest.apirest.models.documents.Categoria;
import com.springboot.reactor.apirest.apirest.models.documents.Producto;
import com.springboot.reactor.apirest.apirest.models.services.ProductoServices;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiRestApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductoServices services;

	@Value(("${config.base.endpoint}"))
    private String Url;

	@Test
	void listarTest() {
		client.get()
				.uri(Url)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Producto.class)
				.hasSize(9);
	}

	/*
	* esta prueba solo funciona cuando se cambia la ruta de la varible url - cambio del api
	@Test
	void listarTest2() {
		client.get()
				.uri(Url)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Producto.class)
                .consumeWith(response -> {
			List<Producto> productos = response.getResponseBody();
			productos.forEach(p -> {
				System.out.println(p.getNombre());
			});

			Assertions.assertThat(productos.size() == 9).isTrue();
		});
	}
	 */


    @Test
    void verDetalleTest() {
        Producto producto = services.findByNombre("TV Samsung").block();

        client.get()
                .uri(Url + "/{id}", Collections.singletonMap("id", producto.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("TV Samsung");
    }

    @Test
    void verDetalle2Test() {
        Producto producto = services.findByNombre("TV Samsung").block();

        client.get()
                .uri(Url + "/{id}", Collections.singletonMap("id", producto.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto p = response.getResponseBody();

                    Assertions.assertThat(p.getNombre()).isEqualTo("TV Samsung");
                    Assertions.assertThat(p.getId()).isNotEmpty();
                });
    }

    @Test
    void crearTest() {

        Categoria categoria = services.findCategoriaByNombre("Muebles").block();

        Producto producto = new Producto("Mesa Comedor", 100000.00, categoria);

        client.post()
                .uri(Url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("Mesa Comedor")
                .jsonPath("$.categoria.nombre").isEqualTo("Muebles");
    }
/**
 * esta prueba solo funciona cuando se cambia la ruta de la varible url - cambio del api
    @Test
    void crear2Test() {

        Categoria categoria = services.findCategoriaByNombre("Muebles").block();

        Producto producto = new Producto("Mesa Comedor", 100000.00, categoria);

        client.post()
                .uri(Url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() { })
                .consumeWith(response -> {
                    Object o = response.getResponseBody().get("producto");
                    Producto p = new ObjectMapper().convertValue(o, Producto.class);
                    Assertions.assertThat(p.getId()).isNotEmpty();
                    Assertions.assertThat(p.getNombre()).isEqualTo("Mesa Comedor");
                    Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
                });
    }

 */

    @Test
    void editarTest() {
        Producto producto = services.findByNombre("TV Samsung").block();
        Categoria categoria = services.findCategoriaByNombre("Electronico").block();
        Producto productoEditado = new Producto("AZUS NoteBook", 900000.00, categoria);

        client.put()
                .uri(Url + "/{id}", Collections.singletonMap("id", producto.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(productoEditado), Producto.class)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo("AZUS NoteBook")
                .jsonPath("$.categoria.nombre").isEqualTo("Electronico");
    }

    @Test
    void eliminarTest() {
        Producto producto = services.findByNombre("APPLE LAPTOP").block();
        client.delete()
                .uri(Url + "/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody()
                .isEmpty();

        client.get()
                .uri(Url + "/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .isEmpty();
    }

}
