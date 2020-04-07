package com.springboot.reactor.apirest.apirest.models.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;

@Document(collection = "categorias")
public class Categoria {

    @Id
    @NotEmpty
    private String id;

    private String Nombre;

    public Categoria() {
    }

    public Categoria(String nombre) {
        Nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }
}
