package co.edu.uptc.simuladorautomatas.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa una momento exacto en el tiempo durante la simulación de una cadena.
 * Guarda el símbolo consumido y el conjunto de estados en los que se encontraba el autómata antes
 * y después de consumir dicho símbolo.
 */
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
}


