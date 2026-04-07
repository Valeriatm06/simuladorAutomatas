package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import co.edu.uptc.simuladorautomatas.logic.EvaluacionCadenaResultado;
import co.edu.uptc.simuladorautomatas.logic.PasoEvaluacion;
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
 * Gestiona la simulación: evaluación de cadenas, reproducción automática y avance de pasos.
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

    public void avanzarManual() {
        if (simulacionActual == null) {
            return;
        }
        detenerPausa();
        reproduccionAutomaticaActiva = false;
        avanzarPaso();
    }

    private void programarSiguientePaso() {
        if (!reproduccionAutomaticaActiva || simulacionActual == null) {
            return;
        }
        detenerPausa();
        pausaSimulacion = new PauseTransition(Duration.millis(800));
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

    private void actualizarTransicionesResaltadas(PasoEvaluacion paso) {
        transicionesResaltadasRuta.clear();
        
        // Si hay transiciones individuales, usarlas; si no, usar el comportamiento anterior
        List<PasoEvaluacion.TransicionIndividual> transiciones = paso.getTransiciones();
        
        if (transiciones != null && !transiciones.isEmpty()) {
            // Hay información de rutas detalladas
            for (PasoEvaluacion.TransicionIndividual transicion : transiciones) {
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

    /**
     * Obtiene el color para una transición específica basado en su ruta
     * @param estadoOrigen Estado de origen de la transición
     * @param estadoDestino Estado de destino de la transición
     * @return Color para la transición, o null si no está resaltada
     */
    public Color obtenerColorTransicion(String estadoOrigen, String estadoDestino) {
        String clave = estadoOrigen + "->" + estadoDestino;
        if (transicionesResaltadasRuta.containsKey(clave)) {
            int numeroRuta = transicionesResaltadasRuta.get(clave);
            return COLORES_RUTAS[numeroRuta % COLORES_RUTAS.length];
        }
        return null;
    }

    /**
     * Verifica si una transición debe estar resaltada
     * @param estadoOrigen Estado de origen
     * @param estadoDestino Estado de destino
     * @return true si la transición debe resaltarse
     */
    public boolean estaTransicionResaltada(String estadoOrigen, String estadoDestino) {
        String clave = estadoOrigen + "->" + estadoDestino;
        return transicionesResaltadasRuta.containsKey(clave);
    }
}
