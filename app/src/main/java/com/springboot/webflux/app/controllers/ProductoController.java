package com.springboot.webflux.app.controllers;

import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.services.ProductoServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SessionAttributes("producto")
@Controller
public class ProductoController {

    @Autowired
    private ProductoServices productoServices;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @GetMapping({"/listar", "/"})
    public Mono<String> listar(Model model) {
        Flux<Producto> productos = productoServices.findAllConNombreUpperCase();
        productos.subscribe(prod -> log.info(prod.getNombre()));
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Lista de productos");
        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Formulario de producto");
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> guardar(Producto producto, SessionStatus status) {
        status.setComplete();
        return productoServices.save(producto).doOnNext(producto1 -> {
            log.info("Producto Guardado:" + producto.getNombre() + " Id: " + producto.getId());
        }).thenReturn("redirect:/listar");
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {
        Mono<Producto> productoMono = productoServices.findById(id).doOnNext(producto -> {
            log.info("Producto: " + producto.getNombre());
        }).defaultIfEmpty(new Producto());
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("producto", productoMono);
        return Mono.just("form");
    }

    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {

        Flux<Producto> productos = productoServices.findAllConNombreUpperCase().delayElements(Duration.ofSeconds(1));
        productos.subscribe(prod -> log.info(prod.getNombre()));
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos,1));
        model.addAttribute("titulo", "Lista de productos");
        return "listar";
    }

    @GetMapping("/listarfull")
    public String listarFull(Model model) {

        Flux<Producto> productos = productoServices.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Lista de productos");
        return "listar";
    }

    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {

        Flux<Producto> productos = productoServices.findAllConNombreUpperCaseRepeat();

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Lista de productos Chunked");
        return "listar-chunked";
    }
}
