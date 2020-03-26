package com.springboot.webflux.app.controllers;

import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.services.ProductoServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Duration;
import java.util.Date;

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
        model.addAttribute("boton", "crear");
        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("boton", "crear");
        model.addAttribute("titulo", "Formulario de producto");
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model, SessionStatus status) {
        if (result.hasErrors()) {
            model.addAttribute("boton", "Guardar");
            model.addAttribute("titulo", "Errores en formulario producto");
            return Mono.just("form");
        } else {
            status.setComplete();
            if (producto.getCreateAt() == null) {
                producto.setCreateAt(new Date());
            }
            return productoServices.save(producto).doOnNext(producto1 -> {
                log.info("Producto Guardado:" + producto.getNombre() + " Id: " + producto.getId());
            }).thenReturn("redirect:/listar?success=producto+guardado+con+exito");
        }
    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editarv2(@PathVariable String id, Model model) {
        return productoServices.findById(id).doOnNext(producto -> {
            log.info("Producto: " + producto.getNombre());
            model.addAttribute("titulo", "Editar Producto");
            model.addAttribute("boton", "editar");
            model.addAttribute("producto", producto);
        })
        .defaultIfEmpty(new Producto())
        .flatMap(producto -> {
            if (producto.getId() == null) {
                return Mono.error(new InterruptedException("No existe el producto"));
            }
            return Mono.just(producto);
        })
        .thenReturn("form")
        .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {
        Mono<Producto> productoMono = productoServices.findById(id).doOnNext(producto -> {
            log.info("Producto: " + producto.getNombre());
        }).defaultIfEmpty(new Producto());
        model.addAttribute("titulo", "Editar Producto");
        model.addAttribute("boton", "editar");
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
