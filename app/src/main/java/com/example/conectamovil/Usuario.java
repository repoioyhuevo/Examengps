package com.example.conectamovil;

import java.util.List;

public class Usuario {
    private String nombre;
    private String apellido;
    private String email;
    private String usuario;
    private String imageUrl;




    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }




    public Usuario() {
        // Constructor vacío necesario para Firebase
    }

    public Usuario(String nombre, String apellido, String email, String usuario, String imageUrl) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.usuario = usuario;
        this.imageUrl = imageUrl;
    }
}
