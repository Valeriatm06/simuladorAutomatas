package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import co.edu.uptc.simuladorautomatas.logic.EvaluacionCadenaResultado;
import co.edu.uptc.simuladorautomatas.logic.PasoEvaluacion;
import co.edu.uptc.simuladorautomatas.logic.TransicionIndividual;
import javafx.animation.PauseTransition;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestor de simulación de autómatas finitos.
 * 
 * Responsable de:
 * - Evaluar palabras/cadenas contra el autómata
 * - Gestionar la reproduccion paso a paso manual o automática
 * - Mantener el estado de la simulación actual
 * - Gestionar colores y resaltados de estados/transiciones
 * 
 */
public class AutomataViewSimulation {
    private final AutomataController controller;
    private Runnable onRedraw;

    // Colores para las diferentes rutas en NFAs
    private static final Color[] COLORES_RUTAS = {
            Color.web("#DC2626"), // Rojo
            Color.web("#2563EB"), // Azul
            Color.web("#059669"), // Verde
            Color.web("#D97706"), // Ámbar
            Color.web("#7C3AED"), // Púrpura
            Color.web("#EC4899"), // Rosa
            Color.web("#14B8A6"), // Turquesa
            Color.web("#F59E0B"), // Naranja
    };

    // Tiempos de animación
    private static final int DURACION_PAUSE_MS = 800;

    private EvaluacionCadenaResultado simulacionActual;
    private int indicePasoActual = -1;
    private PauseTransition pausaSimulacion;
    private boolean reproduccionAutomaticaActiva = false;
    private Boolean ultimoResultadoAceptado;
    private final Set<String> estadosResaltadosEvaluacion = new LinkedHashSet<>();
    private final Set<String> estadosFinalesEvaluacion = new LinkedHashSet<>();
    private List<EvaluacionCadenaResultado> resultadosUltimoLote = new ArrayList<>();
    
    // Transiciones resaltadas con sus rutas asociadas
    private Map<String, Integer> transicionesResaltadasRuta = new HashMap<>();

    public AutomataViewSimulation(AutomataController controller) {
        this.controller = controller;
    }

    public void setRedrawCallback(Runnable callback) {
        this.onRedraw = callback;
    }

    public List<EvaluacionCadenaResultado> evaluarPalabras(String textoPalabras) {
        List<String> entradas = Arrays.stream(textoPalabras.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (entradas.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            controller.validarAutomata();
            List<EvaluacionCadenaResultado> resultados = controller.evaluarLoteConTraza(entradas);
            resultadosUltimoLote = new ArrayList<>(resultados);
            return resultados;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Inicia una simulación para una cadena específica.
     * Prepara el estado para reproducir paso a paso.
     */
    public void iniciarSimulacion(EvaluacionCadenaResultado resultado) {
        detenerSimulacion();
        simulacionActual = resultado;
        indicePasoActual = -1;
        reproduccionAutomaticaActiva = false;
        ultimoResultadoAceptado = null;

        estadosResaltadosEvaluacion.clear();
        estadosFinalesEvaluacion.clear();
        estadosResaltadosEvaluacion.addAll(resultado.getEstadosIniciales());

        if (resultado.getPasos().isEmpty()) {
            aplicarResultadoFinal(null);
            return;
        }

        if (onRedraw != null) {
            onRedraw.run();
        }
    }

    /**
     * Reproduce la simulación desde el inicio de forma automática.
     * Avanza automáticamente por cada paso con retraso.
     */
    public void reproducirDesdeInicio() {
        if (simulacionActual == null) {
            return;
        }
        detenerPausa();
        reproduccionAutomaticaActiva = true;
        indicePasoActual = -1;
        estadosResaltadosEvaluacion.clear();
        estadosFinalesEvaluacion.clear();
        ultimoResultadoAceptado = null;
        estadosResaltadosEvaluacion.addAll(simulacionActual.getEstadosIniciales());
        
        if (onRedraw != null) {
            onRedraw.run();
        }
        
        if (simulacionActual.getPasos().isEmpty()) {
            aplicarResultadoFinal(null);
            return;
        }
        programarSiguientePaso();
    }

    /**
     * Avanza manualmente un paso en la simulación.
     * Detiene cualquier reproducción automática en curso.
     */
    public void avanzarManual() {
        if (simulacionActual == null) {
            return;
        }
        detenerPausa();
        reproduccionAutomaticaActiva = false;
        avanzarPaso();
    }

    /**
     * Programa el siguiente paso en la reproducción automática.
     * Utiliza PauseTransition para crear una demora antes de avanzar.
     */
    private void programarSiguientePaso() {
        if (!reproduccionAutomaticaActiva || simulacionActual == null) {
            return;
        }
        detenerPausa();
        pausaSimulacion = new PauseTransition(Duration.millis(DURACION_PAUSE_MS));
        pausaSimulacion.setOnFinished(event -> {
            avanzarPaso();
            if (reproduccionAutomaticaActiva
                    && simulacionActual != null
                    && indicePasoActual < simulacionActual.getPasos().size() - 1) {
                programarSiguientePaso();
            }
        });
        pausaSimulacion.play();
    }

    /**
     * Avanza un paso en la evaluación de la cadena.
     * Actualiza estados resaltados y transiciones visibles.
     */
    private void avanzarPaso() {
        if (simulacionActual == null) {
            return;
        }
        if (indicePasoActual >= simulacionActual.getPasos().size() - 1) {
            reproduccionAutomaticaActiva = false;
            return;
        }

        indicePasoActual++;
        PasoEvaluacion paso = simulacionActual.getPasos().get(indicePasoActual);

        estadosResaltadosEvaluacion.clear();
        estadosResaltadosEvaluacion.addAll(paso.getEstadosDestino());
        
        // Actualizar transiciones resaltadas con sus colores de ruta
        actualizarTransicionesResaltadas(paso);
        
        if (onRedraw != null) {
            onRedraw.run();
        }

        if (indicePasoActual >= simulacionActual.getPasos().size() - 1) {
            reproduccionAutomaticaActiva = false;
            aplicarResultadoFinal(paso);
        }
    }

    /**
     * Actualiza el mapeo de transiciones resaltadas con sus colores de ruta.
     */
    private void actualizarTransicionesResaltadas(PasoEvaluacion paso) {
        transicionesResaltadasRuta.clear();
        
        // Si hay transiciones individuales, usarlas; si no, usar el comportamiento anterior
        List<TransicionIndividual> transiciones = paso.getTransiciones();
        
        if (transiciones != null && !transiciones.isEmpty()) {
            // Hay información de rutas detalladas
            for (TransicionIndividual transicion : transiciones) {
                String clave = transicion.getEstadoOrigen() + "->" + transicion.getEstadoDestino();
                transicionesResaltadasRuta.put(clave, transicion.getNumeroRuta());
            }
        } else {
            // Comportamiento de compatibilidad: generar rutas automáticamente
            int numeroRuta = 0;
            for (String origen : paso.getEstadosOrigen()) {
                for (String destino : paso.getEstadosDestino()) {
                    String clave = origen + "->" + destino;
                    transicionesResaltadasRuta.put(clave, numeroRuta++);
                }
            }
        }
    }

    /**
     * Aplica el resultado final de la evaluación.
     * Resalta los estados finales y determina si la cadena fue aceptada.
     */
    private void aplicarResultadoFinal(PasoEvaluacion ultimoPaso) {
        if (simulacionActual == null) {
            return;
        }
        ultimoResultadoAceptado = simulacionActual.isAceptada();
        estadosFinalesEvaluacion.clear();

        if (ultimoPaso != null && !ultimoPaso.getEstadosDestino().isEmpty()) {
            estadosFinalesEvaluacion.addAll(ultimoPaso.getEstadosDestino());
        } else if (ultimoPaso != null && !ultimoPaso.getEstadosOrigen().isEmpty()) {
            estadosFinalesEvaluacion.addAll(ultimoPaso.getEstadosOrigen());
        } else {
            estadosFinalesEvaluacion.addAll(simulacionActual.getEstadosIniciales());
        }

        estadosResaltadosEvaluacion.clear();
        
        if (onRedraw != null) {
            onRedraw.run();
        }
    }

    public void detenerSimulacion() {
        detenerPausa();
        reproduccionAutomaticaActiva = false;
        simulacionActual = null;
        indicePasoActual = -1;
        estadosResaltadosEvaluacion.clear();
        estadosFinalesEvaluacion.clear();
        transicionesResaltadasRuta.clear();
        ultimoResultadoAceptado = null;
        
        if (onRedraw != null) {
            onRedraw.run();
        }
    }

    private void detenerPausa() {
        if (pausaSimulacion != null) {
            pausaSimulacion.stop();
            pausaSimulacion = null;
        }
    }

    public Set<String> getEstadosResaltados() {
        return estadosResaltadosEvaluacion;
    }

    public Set<String> getEstadosFinales() {
        return estadosFinalesEvaluacion;
    }

    public Boolean getUltimoResultado() {
        return ultimoResultadoAceptado;
    }

    public EvaluacionCadenaResultado getSimulacionActual() {
        return simulacionActual;
    }

    public int getIndicePaso() {
        return indicePasoActual;
    }

    public void limpiarResultados() {
        resultadosUltimoLote.clear();
    }

    public Color obtenerColorTransicion(String estadoOrigen, String estadoDestino) {
        String clave = estadoOrigen + "->" + estadoDestino;
        if (transicionesResaltadasRuta.containsKey(clave)) {
            int numeroRuta = transicionesResaltadasRuta.get(clave);
            return COLORES_RUTAS[numeroRuta % COLORES_RUTAS.length];
        }
        return null;
    }

    public boolean estaTransicionResaltada(String estadoOrigen, String estadoDestino) {
        String clave = estadoOrigen + "->" + estadoDestino;
        return transicionesResaltadasRuta.containsKey(clave);
    }
}