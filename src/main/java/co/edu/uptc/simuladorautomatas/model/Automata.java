package co.edu.uptc.simuladorautomatas.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Automata {
    private final List<Estado> estados;
    private final List<String> alfabeto;
    private final List<Transicion> transiciones;
    private TipoAutomata tipo;

    public Automata() {
        this(TipoAutomata.DFA);
    }

    public Automata(TipoAutomata tipo) {
        this.estados = new ArrayList<>();
        this.alfabeto = new ArrayList<>();
        this.transiciones = new ArrayList<>();
        this.tipo = tipo;
    }

    public List<Estado> getEstados() {
        return Collections.unmodifiableList(estados);
    }

    public List<String> getAlfabeto() {
        return Collections.unmodifiableList(alfabeto);
    }

    public List<Transicion> getTransiciones() {
        return Collections.unmodifiableList(transiciones);
    }

    public TipoAutomata getTipo() {
        return tipo;
    }

    public void setAlfabeto(List<String> simbolos) {
        alfabeto.clear();
        for (String simbolo : simbolos) {
            String normalizado = SimbolosAutomata.normalizarSimboloTransicion(simbolo);
            if (SimbolosAutomata.esEpsilon(normalizado)) {
                continue;
            }
            if (!normalizado.isEmpty() && !alfabeto.contains(normalizado)) {
                alfabeto.add(normalizado);
            }
        }
    }

    public void agregarEstado(Estado estado) {
        Objects.requireNonNull(estado, "El estado no puede ser nulo");
        if (buscarEstadoPorNombre(estado.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un estado con nombre " + estado.getNombre());
        }
        if (estado.isEsInicial()) {
            limpiarInicial();
        }
        estados.add(estado);
    }

    public void agregarTransicion(Transicion transicion) {
        validarTransicion(transicion);
        transiciones.add(transicion);
    }

    public void eliminarEstado(Estado estado) {
        if (!estados.contains(estado)) {
            throw new IllegalArgumentException("El estado no pertenece al automata");
        }
        
        // Eliminar todas las transiciones que involucren este estado
        transiciones.removeIf(t -> 
            t.getEstadoOrigen().equals(estado) || t.getEstadoDestino().equals(estado)
        );

        estados.remove(estado);
    }

    public Optional<Estado> buscarEstadoPorNombre(String nombre) {
        return estados.stream().filter(e -> e.getNombre().equals(nombre)).findFirst();
    }

    public Estado getEstadoInicial() {
        return estados.stream().filter(Estado::isEsInicial).findFirst().orElse(null);
    }

    private void limpiarInicial() {
        estados.forEach(e -> e.setEsInicial(false));
    }

    private void validarTransicion(Transicion transicion) {
        Objects.requireNonNull(transicion, "La transicion no puede ser nula");
        String simboloNormalizado = prepararSimbolo(transicion);
        validarEstadosExisten(transicion);
        if (SimbolosAutomata.esEpsilon(simboloNormalizado)) {
            validarEpsilonEnDfa();
            return;
        }
        validarPertenenciaAlfabeto(simboloNormalizado);
        validarDeterminismoDfa(transicion, simboloNormalizado);
    }


    private String prepararSimbolo(Transicion transicion) {
        String simboloNormalizado = SimbolosAutomata.normalizarSimboloTransicion(transicion.getSimbolo());
        transicion.setSimbolo(simboloNormalizado);
        return simboloNormalizado;
    }

    private void validarEstadosExisten(Transicion transicion) {
        if (!estados.contains(transicion.getEstadoOrigen()) || !estados.contains(transicion.getEstadoDestino())) {
            throw new IllegalArgumentException("Origen y destino deben existir en el automata");
        }
    }

    private void validarEpsilonEnDfa() {
        if (tipo == TipoAutomata.DFA) {
            throw new IllegalArgumentException("En DFA no se permiten transiciones epsilon/lambda");
        }
    }

    private void validarPertenenciaAlfabeto(String simbolo) {
        if (!alfabeto.contains(simbolo)) {
            throw new IllegalArgumentException("El simbolo " + simbolo + " no pertenece al alfabeto");
        }
    }

    private void validarDeterminismoDfa(Transicion transicion, String simboloNormalizado) {
        if (tipo == TipoAutomata.DFA) {
            boolean existeAmbiguedad = transiciones.stream().anyMatch(t ->
                    t.getEstadoOrigen().equals(transicion.getEstadoOrigen())
                            && t.getSimbolo().equals(simboloNormalizado));

            if (existeAmbiguedad) {
                throw new IllegalArgumentException("En DFA no puede haber transiciones ambiguas para el mismo simbolo");
            }
        }
    }
}

