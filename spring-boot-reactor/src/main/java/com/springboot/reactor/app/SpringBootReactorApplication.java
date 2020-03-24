package com.springboot.reactor.app;

import com.springboot.reactor.app.models.Comentarios;
import com.springboot.reactor.app.models.Usuario;
import com.springboot.reactor.app.models.UsuarioComentarios;
import com.sun.security.auth.UnixNumericUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploIntervaliInfinito();
	}

	public void ejemploIntervaliInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(i -> {
					if (i > 5) {
						return Flux.error(new InterruptedException("Solo hasta 5!"));
					}
					return Flux.just(i);
				})
				.map(i -> "Hola " + i)
				.retry(2)
				.subscribe(s -> log.info(s), e -> log.error(e.getMessage()));
		latch.await();
	}

	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rangos = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));
		//rangos.subscribe();
		rangos.blockLast();
		//Thread.sleep(13000);
	}

	public void ejemploIntervalo() {
		Flux<Integer> rangos = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		rangos.zipWith(retraso, (ra, re) -> ra)
				.doOnNext(i -> log.info(i.toString()))
				.blockLast();
				//.subscribe();
	}

	public void ejemploZipWithRangos() {
		Flux<Integer> range = Flux.range(0, 4);
		Flux.just(1,2,3,4)
				.map(i -> (i*2))
				.zipWith(range, (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
				.subscribe(texto -> log.info(texto));
	}

	public void ejemploUsuarioComentariosZipWithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("whats up!");
			comentarios.addComentario("curso spring boot!");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono.zipWith(comentariosMono).map(tuple -> {
			Usuario u = tuple.getT1();
			Comentarios c = tuple.getT2();
			return new UsuarioComentarios(u, c);
		});
		usuarioComentariosMono.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("whats up!");
			comentarios.addComentario("curso spring boot!");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono.zipWith(comentariosMono, (usuario, comentarioUsuario) -> new UsuarioComentarios(usuario, comentarioUsuario));
		usuarioComentariosMono.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("whats up!");
			comentarios.addComentario("curso spring boot!");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioComentariosMono = usuarioMono.flatMap(u -> comentariosMono.map(c -> new UsuarioComentarios(u, c)));
		usuarioComentariosMono.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploCllectList() throws Exception {
		List<Usuario>  usuariosList = getUsuariosObject();

		Flux.fromIterable(usuariosList)
				.collectList()
				.subscribe(lista -> {
					lista.forEach(item -> log.info(item.toString()));
					});
	}

	public void ejemploToString() throws Exception {
		List<Usuario>  usuariosList = getUsuariosObject();

		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if (nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				})
				.map(nombre -> {
					return nombre.toLowerCase();
				}).subscribe(u -> log.info(u.toString()));
	}

	public void ejemploFlatMap() throws Exception {
		List<String> usuariosList = getUsuarios();

		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if (usuario.getNombre().equalsIgnoreCase("bruce")) {
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				}).subscribe(u -> log.info(u.toString()));
	}

	public void ejemploIterable() throws Exception {
		List<String> usuariosList = getUsuarios();

		//Flux<String> nombres = Flux.just("Andres Guzman", "Diego Fulano", "Carlos Diaz", "Juan Gonzales", "Bruce lee", "Bruce Willie");
		Flux<String> nombres = Flux.fromIterable(usuariosList);

		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce"))
				.doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacíos");
					}
					System.out.println(usuario.getNombre().concat(" ".concat(usuario.getApellido())));
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		usuarios.subscribe(e -> log.info(e.toString()),
				error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable con exito!");
					}
				});
	}

	public List<String> getUsuarios() {
		List<String>  usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Diego Fulano");
		usuariosList.add("Carlos Diaz");
		usuariosList.add("Bruce lee");
		usuariosList.add("Juan Gonzales");
		usuariosList.add("Bruce Willie");
		return usuariosList;
	}

	public List<Usuario> getUsuariosObject() {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Diego", "Fulano"));
		usuariosList.add(new Usuario("Carlos", "Diaz"));
		usuariosList.add(new Usuario("Bruce", "lee"));
		usuariosList.add(new Usuario("Juan", "Gonzales"));
		usuariosList.add(new Usuario("Bruce", "Willie"));
		return usuariosList;
	}

}
