package com.example.conectamovil;

import java.io.Serializable;
import java.util.List;

public class Ruta implements Serializable {
    private List<LatLngWrapper> coordenadas;
    private String direccionInicio;
    private String direccionFin;
    private String distancia;
    private String duracion;
    private String horaInicio;

    public Ruta() {
    }

    public List<LatLngWrapper> getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(List<LatLngWrapper> coordenadas) {
        this.coordenadas = coordenadas;
    }

    public String getDireccionInicio() {
        return direccionInicio;
    }

    public void setDireccionInicio(String direccionInicio) {
        this.direccionInicio = direccionInicio;
    }

    public String getDireccionFin() {
        return direccionFin;
    }

    public void setDireccionFin(String direccionFin) {
        this.direccionFin = direccionFin;
    }

    public String getDistancia() {
        return distancia;
    }

    public void setDistancia(String distancia) {
        this.distancia = distancia;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }
}
