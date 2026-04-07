package co.edu.uptc.simuladorautomatas.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PasoEvaluacion {
    private final String simbolo;
    private final List<String> estadosOrigen;
    private final List<String> estadosDestino;
    private final List<TransicionIndividual> transiciones;

    public PasoEvaluacion(String simbolo, List<String> estadosOrigen, List<String> estadosDestino) {
        this.simbolo = simbolo;
        this.estadosOrigen = Collections.unmodifiableList(new ArrayList<>(estadosOrigen));
        this.estadosDestino = Collections.unmodifiableList(new ArrayList<>(estadosDestino));
        this.transiciones = new ArrayList<>();
    }

    public PasoEvaluacion(String simbolo, List<String> estadosOrigen, List<String> estadosDestino, List<TransicionIndividual> transiciones) {
        this.simbolo = simbolo;
        this.estadosOrigen = Collections.unmodifiableList(new ArrayList<>(estadosOrigen));
        this.estadosDestino = Collections.unmodifiableList(new ArrayList<>(estadosDestino));
        this.transiciones = Collections.unmodifiableList(new ArrayList<>(transiciones));
    }

    public String getSimbolo() {
        return simbolo;
    }

    public List<String> getEstadosOrigen() {
        return estadosOrigen;
    }

    public List<String> getEstadosDestino() {
        return estadosDestino;
    }

    public List<TransicionIndividual> getTransiciones() {
        return transiciones;
    }

    /**
     * Clase que representa una transición individual de origen a destino
     * Se usa para rastrear rutas separadas en NFAs
     */
    public static class TransicionIndividual {
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
}

