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
        if (automata.getEstados().isEmpty()) {
            throw new IllegalStateException("Debe existir al menos un estado");
        }
        if (automata.getEstadoInicial() == null) {
            throw new IllegalStateException("Debe existir un estado inicial");
        }
        Set<String> alfabeto = new HashSet<>(automata.getAlfabeto());
        for (Transicion transicion : automata.getTransiciones()) {
            String simbolo = SimbolosAutomata.normalizarSimboloTransicion(transicion.getSimbolo());
            transicion.setSimbolo(simbolo);
            if (SimbolosAutomata.esEpsilon(simbolo)) {
                if (automata.getTipo() == TipoAutomata.DFA) {
                    throw new IllegalStateException("DFA invalido: no se permiten transiciones epsilon/lambda");
                }
                continue;
            }
            if (!alfabeto.contains(simbolo)) {
                throw new IllegalStateException("La transicion " + transicion + " usa un simbolo fuera del alfabeto");
            }
        }
        if (automata.getTipo() == TipoAutomata.DFA) {
            validarDeterminismo(automata);
        }
    }

    private void validarDeterminismo(Automata automata) {
        Set<String> firma = new HashSet<>();
        for (Transicion transicion : automata.getTransiciones()) {
            String simbolo = SimbolosAutomata.normalizarSimboloTransicion(transicion.getSimbolo());
            if (SimbolosAutomata.esEpsilon(simbolo)) {
                throw new IllegalStateException("DFA invalido: no se permiten transiciones epsilon/lambda");
            }
            String llave = transicion.getEstadoOrigen().getNombre() + "|" + simbolo;
            if (!firma.add(llave)) {
                throw new IllegalStateException("DFA invalido: existe ambiguedad en " + llave);
            }
        }
        for (Estado estado : automata.getEstados()) {
            for (String simbolo : automata.getAlfabeto()) {
                long cantidad = automata.getTransiciones().stream()
                        .filter(t -> t.getEstadoOrigen().equals(estado) && t.getSimbolo().equals(simbolo))
                        .count();
                if (cantidad != 1) {
                    throw new IllegalStateException("DFA incompleto: el estado " + estado + " no tiene transicion unica para " + simbolo);
                }
            }
        }
    }
}

