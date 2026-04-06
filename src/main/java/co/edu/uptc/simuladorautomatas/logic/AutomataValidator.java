package co.edu.uptc.simuladorautomatas.logic;

import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.SimbolosAutomata;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;

import java.util.HashSet;
import java.util.Set;

public class AutomataValidator {

    public void validar(Automata automata) {
        validarEstructuraBasica(automata);
        validarTransicionesYAlfabeto(automata);

        if (automata.getTipo() == TipoAutomata.DFA) {
            validarDeterminismo(automata);
        }
    }

    private void validarEstructuraBasica(Automata automata) {
        if (automata.getEstados().isEmpty()) {
            throw new IllegalStateException("Debe existir al menos un estado");
        }
        if (automata.getEstadoInicial() == null) {
            throw new IllegalStateException("Debe existir un estado inicial");
        }
    }

    private void validarTransicionesYAlfabeto(Automata automata) {
        Set<String> alfabeto = new HashSet<>(automata.getAlfabeto());

        for (Transicion transicion : automata.getTransiciones()) {
            String simboloNormalizado = prepararSimboloTransicion(transicion);

            if (SimbolosAutomata.esEpsilon(simboloNormalizado)) {
                validarEpsilonPermitido(automata);
                continue;
            }

            validarPertenenciaAlfabeto(simboloNormalizado, alfabeto, transicion);
        }
    }

    private String prepararSimboloTransicion(Transicion transicion) {
        String simbolo = SimbolosAutomata.normalizarSimboloTransicion(transicion.getSimbolo());
        transicion.setSimbolo(simbolo);
        return simbolo;
    }

    private void validarEpsilonPermitido(Automata automata) {
        if (automata.getTipo() == TipoAutomata.DFA) {
            throw new IllegalStateException("DFA invalido: no se permiten transiciones epsilon/lambda");
        }
    }

    private void validarPertenenciaAlfabeto(String simbolo, Set<String> alfabeto, Transicion transicion) {
        if (!alfabeto.contains(simbolo)) {
            throw new IllegalStateException("La transicion " + transicion + " usa un simbolo fuera del alfabeto");
        }
    }


    private void validarDeterminismo(Automata automata) {
        validarCeroAmbiguedad(automata);
        validarCompletitud(automata);
    }

    private void validarCeroAmbiguedad(Automata automata) {
        Set<String> firmasDeTransicion = new HashSet<>();

        for (Transicion transicion : automata.getTransiciones()) {
            String simbolo = transicion.getSimbolo();

            if (SimbolosAutomata.esEpsilon(simbolo)) {
                throw new IllegalStateException("DFA invalido: no se permiten transiciones epsilon/lambda");
            }

            String firmaUnica = transicion.getEstadoOrigen().getNombre() + "|" + simbolo;
            if (!firmasDeTransicion.add(firmaUnica)) {
                throw new IllegalStateException("DFA invalido: existe ambiguedad en " + firmaUnica);
            }
        }
    }

    private void validarCompletitud(Automata automata) {
        for (Estado estado : automata.getEstados()) {
            for (String simbolo : automata.getAlfabeto()) {
                long cantidadCaminos = automata.getTransiciones().stream()
                        .filter(t -> t.getEstadoOrigen().equals(estado) && t.getSimbolo().equals(simbolo))
                        .count();

                if (cantidadCaminos != 1) {
                    throw new IllegalStateException("DFA incompleto: el estado " + estado.getNombre() +
                            " no tiene transicion unica para '" + simbolo + "'");
                }
            }
        }
    }
}
