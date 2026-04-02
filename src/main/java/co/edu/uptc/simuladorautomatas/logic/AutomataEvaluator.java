package co.edu.uptc.simuladorautomatas.logic;

import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AutomataEvaluator {

    private final AutomataValidator validator;

    public AutomataEvaluator() {
        this.validator = new AutomataValidator();
    }

    public EvaluacionCadenaResultado evaluar(Automata automata, String cadena, boolean incluirTraza) {
        validator.validar(automata);
        String valor = cadena == null ? "" : cadena.trim();
        if (automata.getTipo() == TipoAutomata.DFA) {
            return evaluarDfa(automata, valor, incluirTraza);
        }
        return evaluarNfa(automata, valor, incluirTraza);
    }

    public List<EvaluacionCadenaResultado> evaluarLote(Automata automata, List<String> cadenas) {
        if (cadenas.size() > 10) {
            throw new IllegalArgumentException("Solo se permiten hasta 10 cadenas por lote");
        }
        List<EvaluacionCadenaResultado> resultados = new ArrayList<>();
        for (String cadena : cadenas) {
            resultados.add(evaluar(automata, cadena, false));
        }
        return resultados;
    }

    private EvaluacionCadenaResultado evaluarDfa(Automata automata, String cadena, boolean incluirTraza) {
        Estado actual = automata.getEstadoInicial();
        List<String> estadosIniciales = List.of(actual.getNombre());
        List<PasoEvaluacion> pasos = new ArrayList<>();
        StringBuilder traza = new StringBuilder(actual.getNombre());
        for (char simbolo : cadena.toCharArray()) {
            String valor = String.valueOf(simbolo);
            Estado siguiente = null;
            for (Transicion transicion : automata.getTransiciones()) {
                if (transicion.getEstadoOrigen().equals(actual) && transicion.getSimbolo().equals(valor)) {
                    siguiente = transicion.getEstadoDestino();
                    break;
                }
            }
            List<String> origenPaso = List.of(actual.getNombre());
            if (siguiente == null) {
                pasos.add(new PasoEvaluacion(valor, origenPaso, List.of()));
                String trazaFinal = incluirTraza ? traza + " -> (sin transicion para '" + valor + "')" : "";
                return new EvaluacionCadenaResultado(cadena, false, trazaFinal, estadosIniciales, pasos);
            }
            pasos.add(new PasoEvaluacion(valor, origenPaso, List.of(siguiente.getNombre())));
            if (incluirTraza) {
                traza.append(" -> (").append(actual.getNombre()).append(", ").append(valor).append(") -> ")
                        .append(siguiente.getNombre());
            }
            actual = siguiente;
        }
        return new EvaluacionCadenaResultado(
                cadena,
                actual.isEsAceptacion(),
                incluirTraza ? traza.toString() : "",
                estadosIniciales,
                pasos
        );
    }

    private EvaluacionCadenaResultado evaluarNfa(Automata automata, String cadena, boolean incluirTraza) {
        Set<Estado> actuales = new LinkedHashSet<>();
        actuales.add(automata.getEstadoInicial());
        List<String> estadosIniciales = nombresEstados(actuales);
        List<PasoEvaluacion> pasos = new ArrayList<>();
        StringBuilder traza = new StringBuilder("{").append(formatoConjunto(actuales)).append("}");

        for (char simbolo : cadena.toCharArray()) {
            String valor = String.valueOf(simbolo);
            Set<Estado> siguientes = new LinkedHashSet<>();
            for (Estado estado : actuales) {
                automata.getTransiciones().stream()
                        .filter(t -> t.getEstadoOrigen().equals(estado) && t.getSimbolo().equals(valor))
                        .map(Transicion::getEstadoDestino)
                        .forEach(siguientes::add);
            }
            pasos.add(new PasoEvaluacion(valor, nombresEstados(actuales), nombresEstados(siguientes)));
            if (incluirTraza) {
                traza.append(" -> (").append(formatoConjunto(actuales)).append(", ").append(valor).append(") -> {")
                        .append(formatoConjunto(siguientes)).append("}");
            }
            actuales = siguientes;
            if (actuales.isEmpty()) {
                return new EvaluacionCadenaResultado(cadena, false, incluirTraza ? traza.toString() : "", estadosIniciales, pasos);
            }
        }

        boolean aceptada = actuales.stream().anyMatch(Estado::isEsAceptacion);
        return new EvaluacionCadenaResultado(cadena, aceptada, incluirTraza ? traza.toString() : "", estadosIniciales, pasos);
    }

    private String formatoConjunto(Set<Estado> estados) {
        return estados.stream().map(Estado::getNombre).collect(Collectors.joining(","));
    }

    private List<String> nombresEstados(Set<Estado> estados) {
        return estados.stream().map(Estado::getNombre).collect(Collectors.toList());
    }
}

