package co.edu.uptc.simuladorautomatas.logic;

import java.util.Objects;

/**
 * Clase que representa una transición individual de origen a destino
 * Se usa para rastrear rutas separadas en NFAs
 */
public class TransicionIndividual {
    private final String estadoOrigen;
    private final String estadoDestino;
    private final int numeroRuta; // Identificador de la ruta para asignar colores

    public TransicionIndividual(String estadoOrigen, String estadoDestino, int numeroRuta) {
        this.estadoOrigen = Objects.requireNonNull(estadoOrigen);
        this.estadoDestino = Objects.requireNonNull(estadoDestino);
        this.numeroRuta = numeroRuta;
    }

    public String getEstadoOrigen() {
        return estadoOrigen;
    }

    public String getEstadoDestino() {
        return estadoDestino;
    }

    public int getNumeroRuta() {
        return numeroRuta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransicionIndividual)) return false;
        TransicionIndividual that = (TransicionIndividual) o;
        return numeroRuta == that.numeroRuta &&
                estadoOrigen.equals(that.estadoOrigen) &&
                estadoDestino.equals(that.estadoDestino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estadoOrigen, estadoDestino, numeroRuta);
    }
}

