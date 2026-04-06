package co.edu.uptc.simuladorautomatas.model;

public class Transicion {
    private Estado estadoOrigen;
    private String simbolo;
    private Estado estadoDestino;

    public Transicion() {
    }

    public Transicion(Estado estadoOrigen, String simbolo, Estado estadoDestino) {
        this.estadoOrigen = estadoOrigen;
        this.simbolo = simbolo;
        this.estadoDestino = estadoDestino;
    }

    public Estado getEstadoOrigen() {
        return estadoOrigen;
    }

    public void setEstadoOrigen(Estado estadoOrigen) {
        this.estadoOrigen = estadoOrigen;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public Estado getEstadoDestino() {
        return estadoDestino;
    }

    @Override
    public String toString() {
        return estadoOrigen + " -dav-" + simbolo + "--> " + estadoDestino;
    }
}

