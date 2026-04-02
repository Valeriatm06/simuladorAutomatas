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

    public void setTipo(TipoAutomata tipo) {
        this.tipo = tipo;
    }

    public void setAlfabeto(List<String> simbolos) {
        alfabeto.clear();
        for (String simbolo : simbolos) {
            String normalizado = simbolo == null ? "" : simbolo.trim();
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

    public void marcarInicial(Estado estado) {
        if (!estados.contains(estado)) {
            throw new IllegalArgumentException("El estado no pertenece al automata");
        }
        limpiarInicial();
        estado.setEsInicial(true);
    }

    public void alternarAceptacion(Estado estado) {
        if (!estados.contains(estado)) {
            throw new IllegalArgumentException("El estado no pertenece al automata");
        }
        estado.setEsAceptacion(!estado.isEsAceptacion());
    }

    public void agregarTransicion(Transicion transicion) {
        validarTransicion(transicion);
        transiciones.add(transicion);
    }

    public void limpiar() {
        estados.clear();
        alfabeto.clear();
        transiciones.clear();
        tipo = TipoAutomata.DFA;
    }

    public Optional<Estado> buscarEstadoPorNombre(String nombre) {
        return estados.stream().filter(e -> e.getNombre().equals(nombre)).findFirst();
    }

    public Estado getEstadoInicial() {
        return estados.stream().filter(Estado::isEsInicial).findFirst().orElse(null);
    }

    public List<Estado> getEstadosAceptacion() {
        return estados.stream().filter(Estado::isEsAceptacion).toList();
    }

    private void limpiarInicial() {
        estados.forEach(e -> e.setEsInicial(false));
    }

    private void validarTransicion(Transicion transicion) {
        Objects.requireNonNull(transicion, "La transicion no puede ser nula");
        if (!estados.contains(transicion.getEstadoOrigen()) || !estados.contains(transicion.getEstadoDestino())) {
            throw new IllegalArgumentException("Origen y destino deben existir en el automata");
        }
        if (!alfabeto.contains(transicion.getSimbolo())) {
            throw new IllegalArgumentException("El simbolo " + transicion.getSimbolo() + " no pertenece al alfabeto");
        }
        if (tipo == TipoAutomata.DFA) {
            boolean existe = transiciones.stream().anyMatch(t ->
                    t.getEstadoOrigen().equals(transicion.getEstadoOrigen())
                            && t.getSimbolo().equals(transicion.getSimbolo()));
            if (existe) {
                throw new IllegalArgumentException("En DFA no puede haber transiciones ambiguas para el mismo simbolo");
            }
        }
    }
}

