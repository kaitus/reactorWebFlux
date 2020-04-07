package com.springboot.reactor.apirest.apirest;

import com.springboot.reactor.apirest.apirest.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler productoHandler) {
        return RouterFunctions.route(RequestPredicates.GET("/api/v2/productos").or(RequestPredicates.GET("/api/v3/productos")),
                productoHandler::listar)
                .andRoute(RequestPredicates.GET("/api/v2/productos/{id}") , productoHandler::ver)
                .andRoute(RequestPredicates.POST("/api/v2/productos"), productoHandler::crear)
                .andRoute(RequestPredicates.PUT("/api/v2/productos/{id}"), productoHandler::editar)
                .andRoute(RequestPredicates.DELETE("/api/v2/productos/{id}"), productoHandler::eliminar)
                .andRoute(RequestPredicates.POST("/api/v2/productos/upload/{id}"), productoHandler::upload)
                .andRoute(RequestPredicates.POST("/api/v2/productos/crear"), productoHandler::crearConFoto);
    }

}
