package co.edu.uptc.simuladorautomatas.model;

import java.util.Objects;

public class Estado {
    private String nombre;
    private boolean esInicial;
    private boolean esAceptacion;
    private double x;
    private double y;

    public Estado() {
    }

    public Estado(String nombre, boolean esInicial, boolean esAceptacion, double x, double y) {
        this.nombre = nombre;
        this.esInicial = esInicial;
        this.esAceptacion = esAceptacion;
        this.x = x;
        this.y = y;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEsInicial() {
        return esInicial;
    }

    public void setEsInicial(boolean esInicial) {
        this.esInicial = esInicial;
    }

    public boolean isEsAceptacion() {
        return esAceptacion;
    }

    public void setEsAceptacion(boolean esAceptacion) {
        this.esAceptacion = esAceptacion;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Estado estado)) {
            return false;
        }
        return Objects.equals(nombre, estado.nombre);
    }
}

