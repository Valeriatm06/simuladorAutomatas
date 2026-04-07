package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import co.edu.uptc.simuladorautomatas.logic.EvaluacionCadenaResultado;
import co.edu.uptc.simuladorautomatas.logic.PasoEvaluacion;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestiona la simulación: evaluación de cadenas, reproducción automática y avance de pasos.
 */
public class AutomataViewSimulation {
    private final AutomataController controller;
    private Runnable onRedraw;


    private EvaluacionCadenaResultado simulacionActual;
    private int indicePasoActual = -1;
    private PauseTransition pausaSimulacion;
    private boolean reproduccionAutomaticaActiva = false;
    private Boolean ultimoResultadoAceptado;
    private final Set<String> estadosResaltadosEvaluacion = new LinkedHashSet<>();
    private final Set<String> estadosFinalesEvaluacion = new LinkedHashSet<>();
    private List<EvaluacionCadenaResultado> resultadosUltimoLote = new ArrayList<>();

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
        
        if (onRedraw != null) {
            onRedraw.run();
        }

        if (indicePasoActual >= simulacionActual.getPasos().size() - 1) {
            reproduccionAutomaticaActiva = false;
            aplicarResultadoFinal(paso);
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
}
