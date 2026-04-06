package co.edu.uptc.simuladorautomatas.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvaluacionCadenaResultado {
    private final String cadena;
    private final boolean aceptada;
    private final String traza;
    private final List<String> estadosIniciales;
    private final List<PasoEvaluacion> pasos;

    public EvaluacionCadenaResultado(
            String cadena,
            boolean aceptada,
            String traza,
            List<String> estadosIniciales,
            List<PasoEvaluacion> pasos
    ) {
        this.cadena = cadena;
        this.aceptada = aceptada;
        this.traza = traza;
        this.estadosIniciales = Collections.unmodifiableList(new ArrayList<>(estadosIniciales));
        this.pasos = Collections.unmodifiableList(new ArrayList<>(pasos));
    }

    public String getCadena() {
        return cadena;
    }

    public boolean isAceptada() {
        return aceptada;
    }

    public String getTraza() {
        return traza;
    }

    public List<String> getEstadosIniciales() {
        return estadosIniciales;
    }

    public List<PasoEvaluacion> getPasos() {
        return pasos;
    }

    public String getEstadoTexto() {
        return aceptada ? "ACEPTADA" : "RECHAZADA";
    }
}
