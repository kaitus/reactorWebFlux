package com.springboot.reactor.client.springbootclient.handler;

import com.springboot.reactor.client.springbootclient.models.Producto;
import com.springboot.reactor.client.springbootclient.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProductoHandler {

    @Autowired
    public ProductoService service;

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.finAll(), Producto.class);
    }

    public Mono<ServerResponse> detalle(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(service.findById(id).flatMap(producto -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters
                .fromValue(producto)))
                .switchIfEmpty(ServerResponse.notFound().build())
                );
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);

        return producto.flatMap(p -> {
            if (p.getCreateAt() == null) {
                p.setCreateAt(new Date());
            }
            return service.save(p);
        }).flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(p)))
                .onErrorResume(error -> {
                    WebClientResponseException responseException = (WebClientResponseException) error;
                    if (responseException.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(responseException.getResponseBodyAsString()));
                    }
                    return Mono.error(responseException);
                });
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");

        return errorHandler(producto
                .flatMap(p -> service.update(p, id))
                .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(id)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(p)))
                );
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(service.delete(id).then(ServerResponse.noContent().build())
                        );
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(request.multipartData().map(multiPart -> multiPart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(filePart -> service.upload(filePart, id))
                .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(id)))
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(p)))
        );
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {
        return response.onErrorResume(error -> {
            WebClientResponseException responseException = (WebClientResponseException) error;
            if (responseException.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "No existe el producto".concat(responseException.getMessage()));
                body.put("timestamp", new Date());
                body.put("status", responseException.getStatusCode().value());
                return ServerResponse.status(HttpStatus.NOT_FOUND).body(BodyInserters.fromValue(body));
            }
            return Mono.error(responseException);
        });
    }
    
}
