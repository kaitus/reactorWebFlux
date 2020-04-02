package com.springboot.webflux.app.controllers;

import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.services.ProductoServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@SessionAttributes("producto")
@Controller
public class ProductoController {

    @Autowired
    private ProductoServices productoServices;

    @Value("${config.uploads.path}")
    private String path;

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @ModelAttribute("categorias")
    public Flux<Categoria> categorias() {
        return productoServices.findAllCategoria();
    }

    @GetMapping("/upload/img/{nombrefoto:.+}")
    public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombrefoto) throws MalformedURLException {
        Path ruta = Paths.get(path).resolve(nombrefoto).toAbsolutePath();
        Resource imagen = new UrlResource(ruta.toUri());

        return Mono.just(
                ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imagen.getFilename() + "\"")
                .body(imagen)
        );
    }

    @GetMapping("/ver/{id}")
    public Mono<String> ver(Model model, @PathVariable String id) {
        return productoServices.findById(id)
                .doOnNext(p -> {
                   model.addAttribute("producto", p);
                   model.addAttribute("titulo", "detalle del producto");
                }).switchIfEmpty(Mono.just(new Producto()))
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }
                    return Mono.just(p);
                }).then(Mono.just("ver"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }

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
    public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model, @RequestPart("file") FilePart file, SessionStatus status) {
        if (result.hasErrors()) {
            model.addAttribute("boton", "Guardar");
            model.addAttribute("titulo", "Errores en formulario producto");
            return Mono.just("form");
        } else {
            status.setComplete();

            Mono<Categoria> categoria = productoServices.findCategoriaById(producto.getCategoria().getId());

            return categoria.flatMap(c -> {
                if (producto.getCreateAt() == null) {
                    producto.setCreateAt(new Date());
                }

                if (!file.filename().isEmpty()){
                    producto.setFoto(UUID.randomUUID().toString() + " " + file.filename()
                            .replace(" ", "")
                            .replace(":","")
                            .replace("\\", ""));
                }
                producto.setCategoria(c);
                return productoServices.save(producto);
            }).doOnNext(producto1 -> {
                log.info("Categoria Guardado:" + producto.getCategoria().getNombre() + " Id: " + producto.getCategoria().getId());
                log.info("Producto Guardado:" + producto.getNombre() + " Id: " + producto.getId());
            }).flatMap(p -> {
                if (!file.filename().isEmpty()){
                    return file.transferTo(new File(path + p.getFoto()));
                } else {
                    return Mono.empty();
                }
            })
            .thenReturn("redirect:/listar?success=producto+guardado+con+exito");
        }
    }

    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id) {
        return productoServices.findById(id).defaultIfEmpty(new Producto())
            .flatMap(producto -> {
                if (producto.getId() == null) {
                    return Mono.error(new InterruptedException("No existe el producto a eliminar"));
                }
                return Mono.just(producto);
            }).flatMap(p -> {
                log.info("Eliminando producto" + p.getNombre());
                log.info("Eliminando producto Id" + p.getId());
                return productoServices.delete(p);
            })
            .thenReturn("redirect:/listar?success=producto+eliminado+con+exito")
            .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));
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
