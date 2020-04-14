package com.springboot.reactor.client.springbootclient;

import com.springboot.reactor.client.springbootclient.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler productoHandler) {
        return RouterFunctions.route(RequestPredicates.GET("/api/client"), productoHandler::listar)
                .andRoute(RequestPredicates.GET("/api/client/{id}"), productoHandler::detalle)
                .andRoute(RequestPredicates.POST("/api/client"), productoHandler::crear)
                .andRoute(RequestPredicates.PUT("/api/client/{id}"), productoHandler::editar)
                .andRoute(RequestPredicates.DELETE("/api/client/{id}"), productoHandler::eliminar)
                .andRoute(RequestPredicates.POST("/api/client/upload/{id}"), productoHandler::upload);
    }
}
