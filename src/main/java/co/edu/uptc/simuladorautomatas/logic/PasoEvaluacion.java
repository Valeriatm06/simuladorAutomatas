package co.edu.uptc.simuladorautomatas.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasoEvaluacion {
    private final String simbolo;
    private final List<String> estadosOrigen;
    private final List<String> estadosDestino;

    public PasoEvaluacion(String simbolo, List<String> estadosOrigen, List<String> estadosDestino) {
        this.simbolo = simbolo;
        this.estadosOrigen = Collections.unmodifiableList(new ArrayList<>(estadosOrigen));
        this.estadosDestino = Collections.unmodifiableList(new ArrayList<>(estadosDestino));
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
}

