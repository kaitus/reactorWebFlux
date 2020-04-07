package com.springboot.reactor.apirest.apirest.models.controllers;

import com.springboot.reactor.apirest.apirest.models.documents.Producto;
import com.springboot.reactor.apirest.apirest.models.services.ProductoServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/productos")
public class ProductoController {

    @Autowired
    private ProductoServices productoServices;

    @Value("${config.uploads.path}")
    private String path;

    @PostMapping("/v2")
    public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto, @RequestPart FilePart file) {
        if (producto.getCreateAt() == null) {
            producto.setCreateAt(new Date());
        }

        producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", ""));

        return file.transferTo(new File(path + producto.getFoto())).then(productoServices.save(producto)).map(p -> ResponseEntity.ok(p));
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart file) {
        return  productoServices.findById(id).flatMap(p -> {
            p.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                    .replace(" ", "")
                    .replace(":", "")
                    .replace("\\", ""));
            return file.transferTo(new File(path + p.getFoto())).then(productoServices.save(p));
        }).map(p -> ResponseEntity.ok(p))
        .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<Producto> listar() {
        return productoServices.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> ver(@PathVariable String id){
        return productoServices.findById(id).map(producto -> ResponseEntity.ok(producto)).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto) {

        Map<String, Object> respuesta = new HashMap<String, Object>();

        return monoProducto.flatMap(producto -> {
            if (producto.getCreateAt() == null) {
                producto.setCreateAt(new Date());
            }
            return productoServices.save(producto).map(p -> {
                respuesta.put("producto", p);
                respuesta.put("mensaje", "producto creado con éxito");
                respuesta.put("timestamp", new Date());
                respuesta.put("status", HttpStatus.OK.value());
                return ResponseEntity.ok(respuesta);
            });
        }).onErrorResume(t -> {
            return Mono.just(t).cast(WebExchangeBindException.class).flatMap(e -> Mono.just(e.getFieldErrors()))
            .flatMapMany(Flux::fromIterable)
            .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
            .collectList()
            .flatMap(list -> {
                respuesta.put("errors", list);
                respuesta.put("timestamp", new Date());
                respuesta.put("status", HttpStatus.BAD_REQUEST.value());
                return Mono.just(ResponseEntity.badRequest().body(respuesta));
            });
        });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Producto>> actualizar(@RequestBody Producto producto, @PathVariable String id) {
        return productoServices.findById(id).flatMap(p -> {
            p.setNombre(producto.getNombre());
            p.setPrecio(producto.getPrecio());
            p.setCategoria(producto.getCategoria());
            return productoServices.save(p);
        }).map(pr -> ResponseEntity.ok(pr))
        .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id){
        return productoServices.findById(id).flatMap(p -> {
            return productoServices.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
        }).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

}
