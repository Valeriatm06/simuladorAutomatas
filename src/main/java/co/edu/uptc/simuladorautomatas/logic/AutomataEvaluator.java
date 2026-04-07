package co.edu.uptc.simuladorautomatas.logic;

import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.SimbolosAutomata;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Encargado de procesar cadenas de entrada y
 * calcular el recorrido y veredicto de aceptación basándose en las reglas teóricas
 * del autómata (DFA o NFA).
 */
public class AutomataEvaluator {

    private final int LIMITE_LOTE = 10;
    private final AutomataValidator validator;

    public AutomataEvaluator() {
        this.validator = new AutomataValidator();
    }

    // --- Punto de Entrada ---

    public EvaluacionCadenaResultado evaluar(Automata automata, String cadena, boolean incluirTraza) {
        validator.validar(automata);
        String valor = SimbolosAutomata.normalizarCadenaEntrada(cadena);

        if (automata.getTipo() == TipoAutomata.DFA) {
            return evaluarDfa(automata, valor, incluirTraza);
        }
        return evaluarNfa(automata, valor, incluirTraza);
    }

    public List<EvaluacionCadenaResultado> evaluarLote(Automata automata, List<String> cadenas) {
        return evaluarLote(automata, cadenas, false);
    }

    public List<EvaluacionCadenaResultado> evaluarLote(Automata automata, List<String> cadenas, boolean incluirTraza) {
        if (cadenas.size() > LIMITE_LOTE) {
            throw new IllegalArgumentException("Solo se permiten hasta " + LIMITE_LOTE + " cadenas por lote");
        }

        List<EvaluacionCadenaResultado> resultados = new ArrayList<>();
        for (String cadena : cadenas) {
            resultados.add(evaluar(automata, cadena, incluirTraza));
        }
        return resultados;
    }

    // Evaluación DFA

    private EvaluacionCadenaResultado evaluarDfa(Automata automata, String cadena, boolean incluirTraza) {
        Estado actual = automata.getEstadoInicial();
        List<String> estadosIniciales = List.of(actual.getNombre());
        List<PasoEvaluacion> pasos = new ArrayList<>();
        StringBuilder traza = new StringBuilder(actual.getNombre());

        for (char simbolo : cadena.toCharArray()) {
            String valor = String.valueOf(simbolo);
            Estado siguiente = moverDfa(automata, actual, valor);

            List<String> origen = List.of(actual.getNombre());
            List<String> destino = siguiente == null ? List.of() : List.of(siguiente.getNombre());

            pasos.add(new PasoEvaluacion(valor, origen, destino));
            registrarTrazaDfa(traza, incluirTraza, actual, valor, siguiente);

            if (siguiente == null) {
                return new EvaluacionCadenaResultado(cadena, false, incluirTraza ? traza.toString() : "", estadosIniciales, pasos);
            }
            actual = siguiente;
        }

        return new EvaluacionCadenaResultado(cadena, actual.isEsAceptacion(), incluirTraza ? traza.toString() : "", estadosIniciales, pasos);
    }

    private Estado moverDfa(Automata automata, Estado actual, String valor) {
        for (Transicion transicion : automata.getTransiciones()) {
            if (transicion.getEstadoOrigen().equals(actual) && transicion.getSimbolo().equals(valor)) {
                return transicion.getEstadoDestino();
            }
        }
        return null;
    }

    private EvaluacionCadenaResultado evaluarNfa(Automata automata, String cadena, boolean incluirTraza) {
        Set<Estado> iniciales = new LinkedHashSet<>();
        iniciales.add(automata.getEstadoInicial());
        List<PasoEvaluacion> pasos = new ArrayList<>();
        StringBuilder traza = new StringBuilder("{").append(formatoConjunto(iniciales)).append("}");

        Set<Estado> actuales = calcularCierreEpsilon(automata, iniciales, pasos, incluirTraza, traza);
        List<String> estadosIniciales = nombresEstados(actuales);

        for (char simbolo : cadena.toCharArray()) {
            String valor = String.valueOf(simbolo);

            // Obtener transiciones individuales para rastrear rutas separadas
            List<TransicionIndividual> transicionesIndividuales = moverNfaConDetalles(automata, actuales, valor);
            Set<Estado> siguientes = moverNfa(automata, actuales, valor);
            
            pasos.add(new PasoEvaluacion(valor, nombresEstados(actuales), nombresEstados(siguientes), transicionesIndividuales));
            registrarTrazaNfa(traza, incluirTraza, actuales, valor, siguientes);

            actuales = calcularCierreEpsilon(automata, siguientes, pasos, incluirTraza, traza);

            if (actuales.isEmpty()) {
                return new EvaluacionCadenaResultado(cadena, false, incluirTraza ? traza.toString() : "", estadosIniciales, pasos);
            }
        }

        boolean aceptada = actuales.stream().anyMatch(Estado::isEsAceptacion);
        return new EvaluacionCadenaResultado(cadena, aceptada, incluirTraza ? traza.toString() : "", estadosIniciales, pasos);
    }

    private List<TransicionIndividual> moverNfaConDetalles(Automata automata, Set<Estado> actuales, String valor) {
        List<TransicionIndividual> transiciones = new ArrayList<>();
        int numeroRuta = 0;
        
        for (Estado estado : actuales) {
            for (Transicion transicion : automata.getTransiciones()) {
                String simboloTransicion = SimbolosAutomata.normalizarSimboloTransicion(transicion.getSimbolo());
                if (transicion.getEstadoOrigen().equals(estado) && simboloTransicion.equals(valor)) {
                    transiciones.add(new TransicionIndividual(
                            estado.getNombre(),
                            transicion.getEstadoDestino().getNombre(),
                            numeroRuta++
                    ));
                }
            }
        }
        return transiciones;
    }

    private Set<Estado> moverNfa(Automata automata, Set<Estado> actuales, String valor) {
        Set<Estado> siguientes = new LinkedHashSet<>();
        for (Estado estado : actuales) {
            for (Transicion transicion : automata.getTransiciones()) {
                String simboloTransicion = SimbolosAutomata.normalizarSimboloTransicion(transicion.getSimbolo());
                if (transicion.getEstadoOrigen().equals(estado) && simboloTransicion.equals(valor)) {
                    siguientes.add(transicion.getEstadoDestino());
                }
            }
        }
        return siguientes;
    }

    private Set<Estado> calcularCierreEpsilon(Automata automata, Set<Estado> base, List<PasoEvaluacion> pasos, boolean incluirTraza, StringBuilder traza) {
        Set<Estado> cierre = new LinkedHashSet<>(base);
        Deque<Estado> pendientes = new ArrayDeque<>(base);

        while (!pendientes.isEmpty()) {
            Estado actual = pendientes.pop();

            for (Transicion transicion : automata.getTransiciones()) {
                String simbolo = SimbolosAutomata.normalizarSimboloTransicion(transicion.getSimbolo());

                boolean esEstadoActual = transicion.getEstadoOrigen().equals(actual);
                boolean esTransicionEpsilon = SimbolosAutomata.esEpsilon(simbolo);

                if (esEstadoActual && esTransicionEpsilon) {
                    Estado destino = transicion.getEstadoDestino();

                    if (cierre.add(destino)) {
                        pasos.add(new PasoEvaluacion(SimbolosAutomata.EPSILON, List.of(actual.getNombre()), List.of(destino.getNombre())));
                        registrarTrazaEpsilon(traza, incluirTraza, actual, destino);
                        pendientes.push(destino);
                    }
                }
            }
        }
        return cierre;
    }


    private void registrarTrazaDfa(StringBuilder traza, boolean incluirTraza, Estado actual, String valor, Estado siguiente) {
        if (!incluirTraza) return;
        String nombreDestino = (siguiente == null) ? "{}" : siguiente.getNombre();
        traza.append(" -> (").append(actual.getNombre()).append(", ").append(valor).append(") -> ").append(nombreDestino);
    }

    private void registrarTrazaNfa(StringBuilder traza, boolean incluirTraza, Set<Estado> actuales, String valor, Set<Estado> siguientes) {
        if (!incluirTraza) return;
        traza.append(" -> (").append(formatoConjunto(actuales)).append(", ").append(valor).append(") -> {")
                .append(formatoConjunto(siguientes)).append("}");
    }

    private void registrarTrazaEpsilon(StringBuilder traza, boolean incluirTraza, Estado actual, Estado destino) {
        if (!incluirTraza) return;
        traza.append(" -> (").append(actual.getNombre()).append(", ").append(SimbolosAutomata.EPSILON)
                .append(") -> {").append(destino.getNombre()).append("}");
    }

    private List<String> nombresEstados(Collection<Estado> estados) {
        return estados.stream().map(Estado::getNombre).toList();
    }

    private String formatoConjunto(Set<Estado> estados) {
        return estados.stream().map(Estado::getNombre).collect(Collectors.joining(","));
    }

    //Genera la función de transición extendida (δ*) para visualizar la evaluación
    public String generarFuncionTransicionExtendida(EvaluacionCadenaResultado resultado) {
        if (resultado == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String cadena = resultado.getCadena().isEmpty() ? "ε" : resultado.getCadena();
        sb.append("═══════════════════════════════════════\n");
        sb.append("Función de Transición Extendida δ*\n");
        sb.append("═══════════════════════════════════════\n\n");
        sb.append("Cadena: \"").append(cadena).append("\"\n");
        sb.append("Resultado: ").append(resultado.getEstadoTexto()).append("\n\n");

        // Estado inicial
        List<String> estadosIniciales = resultado.getEstadosIniciales();
        String estadosInicStr = estadosIniciales.isEmpty() ? "∅" : "{" + String.join(",", estadosIniciales) + "}";
        sb.append("δ*(").append(estadosInicStr).append(", ε) = ").append(estadosInicStr).append("\n");

        // Cada paso
        List<PasoEvaluacion> pasos = resultado.getPasos();
        for (int i = 0; i < pasos.size(); i++) {
            PasoEvaluacion paso = pasos.get(i);
            String simbolo = paso.getSimbolo();
            if (SimbolosAutomata.esEpsilon(simbolo)) {
                simbolo = "ε";
            }

            // Construir cadena hasta este paso
            String cadenaHasta = cadena.substring(0, Math.min(i + 1, cadena.length()));
            if (cadenaHasta.isEmpty()) {
                cadenaHasta = "ε";
            }

            String origenStr = paso.getEstadosOrigen().isEmpty() ? "∅" : "{" + String.join(",", paso.getEstadosOrigen()) + "}";
            String destinoStr = paso.getEstadosDestino().isEmpty() ? "∅" : "{" + String.join(",", paso.getEstadosDestino()) + "}";

            sb.append("δ*(").append(origenStr).append(", ");
            sb.append(simbolo).append(") = ").append(destinoStr).append("\n");
        }

        sb.append("\n═══════════════════════════════════════\n");
        return sb.toString();
    }
}