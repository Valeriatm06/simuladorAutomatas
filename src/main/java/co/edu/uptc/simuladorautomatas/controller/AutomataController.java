package co.edu.uptc.simuladorautomatas.controller;

import co.edu.uptc.simuladorautomatas.logic.AutomataEvaluator;
import co.edu.uptc.simuladorautomatas.logic.AutomataValidator;
import co.edu.uptc.simuladorautomatas.logic.EvaluacionCadenaResultado;
import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;
import co.edu.uptc.simuladorautomatas.persistence.AutomataJsonRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AutomataController {
    private final AutomataEvaluator evaluator;
    private final AutomataValidator validator;
    private final AutomataJsonRepository repository;
    private Automata automataActual;

    public AutomataController() {
        this.evaluator = new AutomataEvaluator();
        this.validator = new AutomataValidator();
        this.repository = new AutomataJsonRepository();
        this.automataActual = new Automata(TipoAutomata.DFA);
    }

    public Automata getAutomataActual() {
        return automataActual;
    }

    public void nuevoAutomata(TipoAutomata tipo, List<String> alfabeto) {
        this.automataActual = new Automata(tipo);
        this.automataActual.setAlfabeto(alfabeto);
    }

    public void agregarEstado(String nombre, boolean inicial, boolean aceptacion, double x, double y) {
        Estado estado = new Estado(nombre, inicial, aceptacion, x, y);
        automataActual.agregarEstado(estado);
    }

    public void marcarInicial(String nombreEstado) {
        Estado estado = buscarEstado(nombreEstado);
        automataActual.marcarInicial(estado);
    }

    public void alternarAceptacion(String nombreEstado) {
        Estado estado = buscarEstado(nombreEstado);
        automataActual.alternarAceptacion(estado);
    }

    public void agregarTransicion(String origen, String simbolo, String destino) {
        Estado estadoOrigen = buscarEstado(origen);
        Estado estadoDestino = buscarEstado(destino);
        automataActual.agregarTransicion(new Transicion(estadoOrigen, simbolo.trim(), estadoDestino));
    }

    public List<EvaluacionCadenaResultado> evaluarLote(List<String> cadenas) {
        List<String> entradas = cadenas.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
        return evaluator.evaluarLote(automataActual, entradas);
    }

    public EvaluacionCadenaResultado evaluarConTraza(String cadena) {
        return evaluator.evaluar(automataActual, cadena, true);
    }

    public void validarAutomata() {
        validator.validar(automataActual);
    }

    public void guardar(Path path) throws IOException {
        repository.guardar(path, automataActual);
    }

    public void cargar(Path path) throws IOException {
        automataActual = repository.cargar(path);
    }

    public List<String> nombresEstados() {
        List<String> nombres = new ArrayList<>();
        automataActual.getEstados().forEach(e -> nombres.add(e.getNombre()));
        return nombres;
    }

    public void eliminarEstado(String nombreEstado) {
        Estado estado = buscarEstado(nombreEstado);
        automataActual.eliminarEstado(estado);
    }

    private Estado buscarEstado(String nombre) {
        return automataActual.buscarEstadoPorNombre(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Estado no encontrado: " + nombre));
    }
}

