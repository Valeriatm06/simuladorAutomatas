package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import co.edu.uptc.simuladorautomatas.logic.EvaluacionCadenaResultado;
import co.edu.uptc.simuladorautomatas.logic.PasoEvaluacion;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vista principal del simulador de automatas.
 * 
 * Orquesta toda la interfaz gráfica coordinando:
 * - Motor de dibujo (renderización de estados y transiciones)
 * - Manejador de interacción (eventos del canvas y diálogos)
 * - Gestor de simulación (evaluación de cadenas y pasos)
 * - Operaciones de archivo (guardar/cargar)
 * 
 * Maneja:
 * - Ciclo de vida del autómata: creación, edición, evaluación
 * - Entrada de datos: tipo, alfabeto, palabras de prueba
 * - Visualización: canvas con estados/transiciones, resultados
 * - Estado del proceso: indicadores, mensajes de error/éxito
 */
public class AutomataView {
    // Constantes visuales
    private static final String EPSILON_VISUAL = "ε";
    private static final double CANVAS_WIDTH = 900;
    private static final double CANVAS_HEIGHT = 620;
    private static final double MARGEN_ESTADO = 46;
    
    // Constantes de UI
    private static final int MARGIN_TOOLBAR = 8;
    private static final int MARGIN_PANEL_DERECHO = 8;
    private static final int STEP_INICIAL = 1;
    private static final int STEP_CREAR_ESTADO = 2;
    private static final int STEP_CREAR_TRANSICION = 3;
    private static final int STEP_EVALUAR = 4;

    private final Stage stage;
    private final AutomataController controller;

    // Componentes UI
    private Pane panelDibujo;
    private Label estadoProcesoLabel;
    private TextField alfabetoField;
    private TextArea palabrasArea;
    private ListView<EvaluacionCadenaResultado> resultadosLoteList;
    private Button btnSiguientePaso;
    private Button btnReproducir;
    private Button btnVerFuncion;
    private ComboBox<TipoAutomata> tipoCombo;
    private VBox panelConfiguracion;
    private VBox panelPruebas;
    private final VBox[] stepBoxes = new VBox[4];

    // Clases de soporte
    private AutomataViewUIBuilder uiBuilder;
    private AutomataViewDrawing drawingEngine;
    private AutomataViewInteraction interactionHandler;
    private AutomataViewSimulation simulationManager;
    private AutomataViewFileOps fileOps;

    // Estado UI
    private String estadoSeleccionadoCanvas;
    private String estadoArrastrandose;
    private double ultimaXMouse;
    private double ultimaYMouse;
    private boolean modoCrearEstado;

    // Constructor de la vista principal.
    public AutomataView(Stage stage) {
        this.stage = stage;
        this.controller = new AutomataController();
    }

    /**
     * Construye y retorna el árbol de nodos de la interfaz gráfica.
     * 
     * Estructura:
     * - Top: Encabezado (header) con título y botón reinicio
     * - Center: Zona principal con canvas y paneles
     */
    public Parent build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root-with-grid");
        root.setTop(crearHeader());
        root.setCenter(crearZonaPrincipal());
        return root;
    }

    private Parent crearHeader() {
        uiBuilder = new AutomataViewUIBuilder();
        return uiBuilder.crearHeader(() -> fileOps.confirmarReinicio());
    }

    private Parent crearZonaPrincipal() {
        BorderPane contenedor = new BorderPane();
        contenedor.setPadding(new Insets(12));
        agregarToolbar(contenedor);
        configurarCanvas(contenedor);
        agregarPanelDerecho(contenedor);
        return contenedor;
    }

    private void agregarToolbar(BorderPane contenedor) {
        VBox toolbar = uiBuilder.crearToolbarFlotante(
                this::activarModoCreacionEstado,
                this::activarModoCreacionTransicion,
                this::eliminarElementoSeleccionado,
                () -> fileOps.guardarAutomata(),
                () -> fileOps.cargarAutomata(),
                () -> fileOps.mostrarAyuda()
        );
        contenedor.setLeft(toolbar);
        BorderPane.setMargin(toolbar, new Insets(0, 8, 0, 0));
    }

    private void configurarCanvas(BorderPane contenedor) {
        panelDibujo = new Pane();
        panelDibujo.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        panelDibujo.getStyleClass().add("canvas-pane");
        configurarEventosCanvas();
        animarEntradaCanvas();
        contenedor.setCenter(panelDibujo);
    }

    private void configurarEventosCanvas() {
        panelDibujo.setOnMouseClicked(e -> onCanvasMouseClicked(e.getX(), e.getY()));
        panelDibujo.setOnMousePressed(e -> onCanvasMousePressed(e.getX(), e.getY()));
        panelDibujo.setOnMouseDragged(e -> onCanvasMouseDragged(e.getX(), e.getY()));
        panelDibujo.setOnMouseReleased(e -> onCanvasMouseReleased());
        panelDibujo.widthProperty().addListener((o, ol, nw) -> redibujar());
        panelDibujo.heightProperty().addListener((o, ol, nw) -> redibujar());
    }

    private void animarEntradaCanvas() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), panelDibujo);
        fadeIn.setFromValue(0.8);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void agregarPanelDerecho(BorderPane contenedor) {
        inicializarComponentesUI();
        VBox[] panelConfigOut = new VBox[1];
        VBox[] panelPruebasOut = new VBox[1];

        StackPane panelDetalles = uiBuilder.crearPanelDetalles(
                tipoCombo, alfabetoField, this::crearNuevoAutomata,
                palabrasArea, this::evaluarPalabras,
                () -> palabrasArea.clear(), this::agregarEpsilon,
                resultadosLoteList, btnSiguientePaso, btnReproducir, btnVerFuncion,
                this::avanzarSimulacionManual, this::reproducirDesdeInicio, this::mostrarFuncionTransicion,
                estadoProcesoLabel, panelConfigOut, panelPruebasOut
        );

        panelConfiguracion = panelConfigOut[0];
        panelPruebas = panelPruebasOut[0];
        contenedor.setRight(panelDetalles);
        BorderPane.setMargin(panelDetalles, new Insets(0, 0, 0, 8));
    }

    private void inicializarComponentesUI() {
        crearControlesBase();
        configurarListaResultados();
        inicializarServicios();
        configurarCallbacks();
        uiBuilder.crearStepperVisual(stepBoxes);
    }

    private void crearControlesBase() {
        tipoCombo = new ComboBox<>();
        alfabetoField = new TextField();
        palabrasArea = new TextArea();
        resultadosLoteList = new ListView<>();
        btnSiguientePaso = new Button("Siguiente paso");
        btnReproducir = new Button("Reproducir");
        btnVerFuncion = new Button("Ver δ*");
        estadoProcesoLabel = new Label("Seleccione una cadena de los resultados");
    }

    private void configurarListaResultados() {
        resultadosLoteList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EvaluacionCadenaResultado item, boolean empty) {
                super.updateItem(item, empty);
                setText(obtenerTextoResultado(item, empty, getIndex()));
            }
        });
        resultadosLoteList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                reproducirCadenaSeleccionadaLote();
            }
        });
    }

    private String obtenerTextoResultado(EvaluacionCadenaResultado item, boolean empty, int index) {
        if (empty || item == null) {
            return null;
        }
        String valorCadena = item.getCadena().isEmpty() ? "ε" : item.getCadena();
        return ("%d. %s -> %s").formatted(index + 1, valorCadena, item.getEstadoTexto());
    }

    private void inicializarServicios() {
        drawingEngine = new AutomataViewDrawing(panelDibujo);
        interactionHandler = new AutomataViewInteraction(controller, drawingEngine);
        simulationManager = new AutomataViewSimulation(controller);
        drawingEngine.setSimulationManager(simulationManager);
        fileOps = new AutomataViewFileOps(stage, controller);
    }

    private void configurarCallbacks() {
        interactionHandler.setOnStatusChange(this::redibujar);
        simulationManager.setRedrawCallback(this::onSimulationFrameChanged);
        fileOps.setOnAutomataLoaded(this::onAutomataLoaded);
        fileOps.setOnAutomataReset(this::onAutomataReset);
    }

    private void onSimulationFrameChanged() {
        redibujar();
        actualizarEstadoPasoAPaso();
    }

    //Maneja click en canvas: crear estado o seleccionar estado.
    private void onCanvasMouseClicked(double x, double y) {
        if (modoCrearEstado) {
            crearEstadoEnCanvas(x, y);
            return;
        }
        manejarSeleccionEstado(x, y);
    }

    private void manejarSeleccionEstado(double x, double y) {
        String estado = interactionHandler.seleccionarEstadoEnCanvas(x, y);
        if (estado == null) {
            limpiarSeleccionCanvas();
            return;
        }
        estadoSeleccionadoCanvas = estado;
        mostrarInfoEstado("Estado seleccionado: " + estado);
        redibujar();
    }

    private void limpiarSeleccionCanvas() {
        if (estadoSeleccionadoCanvas == null) {
            return;
        }
        estadoSeleccionadoCanvas = null;
        redibujar();
    }

    /**
     * Inicia drag (arrastre) de un estado cuando se presiona con mouse.
     * 
     * Registra la posición inicial y el estado bajo el cursor.
     * Si no hay estado, no hace nada 
     */
    private void onCanvasMousePressed(double x, double y) {
        if (modoCrearEstado) {
            return;
        }
        var estado = interactionHandler.encontrarEstadoEnPosicion(x, y);
        if (estado == null) {
            return;
        }
        estadoArrastrandose = estado.getNombre();
        ultimaXMouse = x;
        ultimaYMouse = y;
    }

    /**
     * Mueve un estado arrastrándolo dentro del canvas.
     * 
     * Calcula delta desde última posición y actualiza coordenadas del estado.
     * Limita movimiento dentro de los márgenes del canvas.
     */
    private void onCanvasMouseDragged(double x, double y) {
        if (estadoArrastrandose == null) {
            return;
        }
        var estado = buscarEstadoArrastrado();
        estado.ifPresent(value -> moverEstado(value, x, y));
    }

    private java.util.Optional<co.edu.uptc.simuladorautomatas.model.Estado> buscarEstadoArrastrado() {
        return controller.getAutomataActual().getEstados().stream()
                .filter(e -> e.getNombre().equals(estadoArrastrandose))
                .findFirst();
    }

    private void moverEstado(co.edu.uptc.simuladorautomatas.model.Estado estado, double x, double y) {
        double deltaX = (x - ultimaXMouse) / drawingEngine.getEscala();
        double deltaY = (y - ultimaYMouse) / drawingEngine.getEscala();
        estado.setX(limitar(estado.getX() + deltaX, MARGEN_ESTADO, CANVAS_WIDTH - MARGEN_ESTADO));
        estado.setY(limitar(estado.getY() + deltaY, MARGEN_ESTADO, CANVAS_HEIGHT - MARGEN_ESTADO));
        ultimaXMouse = x;
        ultimaYMouse = y;
        redibujar();
    }

    private double limitar(double valor, double min, double max) {
        return Math.max(min, Math.min(max, valor));
    }

    private void onCanvasMouseReleased() {
        if (estadoArrastrandose == null) {
            return;
        }
        mostrarInfoEstado("Estado '" + estadoArrastrandose + "' movido");
        estadoArrastrandose = null;
    }

    /**
     * Crea un nuevo autómata con el tipo y alfabeto especificado.
     * 
     * Valida entrada, crea autómata, transiciona a panel de pruebas.
     */
    private void crearNuevoAutomata() {
        try {
            List<String> alfabeto = construirAlfabeto();
            controller.nuevoAutomata(tipoCombo.getValue(), alfabeto);
            simulationManager.detenerSimulacion();
            mostrarAutmataCreado();
        } catch (Exception ex) {
            mostrarErrorEstado("Error: " + ex.getMessage());
        }
    }

    private List<String> construirAlfabeto() {
        String alfabetoText = alfabetoField.getText().trim();
        if (alfabetoText.isEmpty()) {
            throw new IllegalArgumentException("Ingrese al menos un símbolo en el alfabeto (ej: a,b)");
        }
        List<String> alfabeto = Arrays.stream(alfabetoText.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (alfabeto.isEmpty()) {
            throw new IllegalArgumentException("Ingrese al menos un símbolo en el alfabeto (ej: a,b)");
        }
        return alfabeto;
    }

    private void mostrarAutmataCreado() {
        redibujar();
        mostrarInfoEstado("Autómata " + tipoCombo.getValue() + " creado. Agregue estados con el toolbar.");
        uiBuilder.mostrarPanelPruebas(panelConfiguracion, panelPruebas);
        marcarPasoCompleto(1);
    }

    /**
     * Activa modo de creación de estados.
     * 
     * Muestra diálogo para ingresar nombre del estado.
     * Al confirmar, usuario puede hacer click en canvas para posicionar.
     */
    private void activarModoCreacionEstado() {
        interactionHandler.mostrarDialogoNuevoEstado(() -> {
            modoCrearEstado = true;
            mostrarInfoEstado("Click en canvas para ubicar el estado");
        });
    }

    /**
     * Activa modo de creación de transiciones.
     * 
     * Al confirmar, agrega transición y redibuja.
     */
    private void activarModoCreacionTransicion() {
        interactionHandler.mostrarDialogoNuevaTransicion(() -> {
            redibujar();
            marcarPasoCompleto(3);
        });
    }

    private void eliminarElementoSeleccionado() {
        interactionHandler.eliminarEstado(estadoSeleccionadoCanvas);
        estadoSeleccionadoCanvas = null;
        redibujar();
    }

    private void crearEstadoEnCanvas(double x, double y) {
        String nombreEstado = interactionHandler.getEstadoEnCreacionNombre();
        if (nombreEstado == null || nombreEstado.isEmpty()) {
            mostrarErrorEstado("Ingrese el nombre del estado antes de hacer click");
            return;
        }
        crearEstadoYActualizarVista(nombreEstado, x, y);
    }

    private void crearEstadoYActualizarVista(String nombreEstado, double x, double y) {
        try {
            interactionHandler.crearEstadoEnPosicion(
                    nombreEstado,
                    interactionHandler.isEstadoEnCreacionInicial(),
                    interactionHandler.isEstadoEnCreacionAceptacion(),
                    x,
                    y
            );
            finalizarCreacionEstado(nombreEstado);
        } catch (Exception ex) {
            mostrarErrorEstado(ex.getMessage());
        }
    }

    private void finalizarCreacionEstado(String nombreEstado) {
        modoCrearEstado = false;
        estadoSeleccionadoCanvas = nombreEstado;
        interactionHandler.limpiarEstadoEnCreacion();
        redibujar();
        marcarPasoCompleto(2);
        mostrarInfoEstado("Estado '" + nombreEstado + "' creado");
    }

    /**
     * Evalúa todas las palabras ingresadas en el TextArea contra el autómata.
     * 
     * Procesa líneas, filtra vacías, simula y muestra resultados en ListView.
     * Habilita botones de paso a paso y reproducción.
     */
    private void evaluarPalabras() {
        try {
            List<EvaluacionCadenaResultado> resultados = simulationManager.evaluarPalabras(palabrasArea.getText());
            actualizarResultadosEvaluacion(resultados);
        } catch (Exception ex) {
            mostrarErrorEstado(ex.getMessage());
        }
    }

    private void actualizarResultadosEvaluacion(List<EvaluacionCadenaResultado> resultados) {
        resultadosLoteList.getItems().setAll(resultados);
        seleccionarPrimerResultado(resultados);
        long aceptadas = resultados.stream().filter(EvaluacionCadenaResultado::isAceptada).count();
        mostrarMensajeEvaluacion(resultados.size(), aceptadas);
        marcarPasoCompleto(4);
    }

    private void seleccionarPrimerResultado(List<EvaluacionCadenaResultado> resultados) {
        if (!resultados.isEmpty()) {
            resultadosLoteList.getSelectionModel().select(0);
        }
    }

    private void mostrarMensajeEvaluacion(int total, long aceptadas) {
        limpiarEstilosEstadoProceso();
        estadoProcesoLabel.getStyleClass().add("status-ok");
        estadoProcesoLabel.setText("Evaluadas " + total + " cadenas. " + aceptadas
                + " aceptadas. Seleccione una para ver paso a paso.");
    }

    private void reproducirCadenaSeleccionadaLote() {
        EvaluacionCadenaResultado seleccionado = resultadosLoteList.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarErrorEstado("Seleccione una cadena para ver su paso a paso");
            return;
        }
        iniciarReproduccionSeleccionada(seleccionado);
    }

    private void iniciarReproduccionSeleccionada(EvaluacionCadenaResultado seleccionado) {
        simulationManager.iniciarSimulacion(seleccionado);
        btnSiguientePaso.setDisable(false);
        btnReproducir.setDisable(false);
        btnVerFuncion.setDisable(false);
        actualizarEstadoPasoAPaso();
        redibujar();
    }

    private void avanzarSimulacionManual() {
        simulationManager.avanzarManual();
        actualizarEstadoPasoAPaso();
        redibujar();
    }

    private void reproducirDesdeInicio() {
        simulationManager.reproducirDesdeInicio();
        actualizarEstadoPasoAPaso();
        redibujar();
    }

    private void mostrarFuncionTransicion() {
        EvaluacionCadenaResultado simulacion = simulationManager.getSimulacionActual();
        if (simulacion == null) {
            return;
        }

        co.edu.uptc.simuladorautomatas.logic.AutomataEvaluator evaluator = 
                new co.edu.uptc.simuladorautomatas.logic.AutomataEvaluator();
        String funcionExtendida = evaluator.generarFuncionTransicionExtendida(simulacion);
        VentanaFuncionTransicion.mostrar(funcionExtendida);
    }

    /**
     * Actualiza el Label de estado indicando el paso actual de la evaluación.
     * Estilos: status-ok (paso normal) o status-error (rechazada).
     */
    private void actualizarEstadoPasoAPaso() {
        EvaluacionCadenaResultado simulacion = simulationManager.getSimulacionActual();
        if (simulacion == null) {
            return;
        }

        int indicePaso = simulationManager.getIndicePaso();
        List<PasoEvaluacion> pasos = simulacion.getPasos();
        String cadenaMostrada = simulacion.getCadena().isEmpty() ? EPSILON_VISUAL : simulacion.getCadena();

        limpiarEstilosEstadoProceso();

        if (indicePaso < 0) {
            estadoProcesoLabel.getStyleClass().add("status-ok");
            estadoProcesoLabel.setText("Cadena: " + cadenaMostrada + " | Inicio en "
                    + formatearConjuntoEstados(simulacion.getEstadosIniciales()));
            return;
        }

        PasoEvaluacion paso = pasos.get(indicePaso);
        String simbolo = formatearSimboloPaso(paso.getSimbolo());
        String origen = formatearConjuntoEstados(paso.getEstadosOrigen());
        String destino = formatearConjuntoEstados(paso.getEstadosDestino());

        boolean pasoFinal = indicePaso >= pasos.size() - 1;
        if (pasoFinal && simulationManager.getUltimoResultado() != null) {
            boolean aceptada = simulationManager.getUltimoResultado();
            estadoProcesoLabel.getStyleClass().add(aceptada ? "status-ok" : "status-error");
            estadoProcesoLabel.setText("Cadena: " + cadenaMostrada
                    + " | Paso " + (indicePaso + 1)
                    + ": (" + origen + ", " + simbolo + ") -> " + destino
                    + " | " + (aceptada ? "ACEPTADA" : "RECHAZADA"));
            return;
        }

        estadoProcesoLabel.getStyleClass().add("status-ok");
        estadoProcesoLabel.setText("Cadena: " + cadenaMostrada
                + " | Paso " + (indicePaso + 1)
                + ": (" + origen + ", " + simbolo + ") -> " + destino);
    }

    private String formatearSimboloPaso(String simbolo) {
        if (simbolo == null || simbolo.isBlank()) {
            return EPSILON_VISUAL;
        }
        String valor = simbolo.trim();
        if (valor.equals(EPSILON_VISUAL) || valor.equalsIgnoreCase("epsilon") || valor.equalsIgnoreCase("lambda") || valor.equals("λ")) {
            return EPSILON_VISUAL;
        }
        return valor;
    }

    private String formatearConjuntoEstados(List<String> estados) {
        if (estados == null || estados.isEmpty()) {
            return "{}";
        }
        if (estados.size() == 1) {
            return estados.get(0);
        }
        return "{" + String.join(", ", estados) + "}";
    }

    private void redibujar() {
        drawingEngine.redibujar(
                controller.getAutomataActual(),
                estadoSeleccionadoCanvas,
                simulationManager.getEstadosResaltados(),
                simulationManager.getEstadosFinales(),
                simulationManager.getUltimoResultado()
        );
    }

    private void mostrarInfoEstado(String mensaje) {
        estadoProcesoLabel.setText(mensaje);
        estadoProcesoLabel.getStyleClass().removeAll("status-error");
        estadoProcesoLabel.getStyleClass().add("status-ok");
    }

    private void mostrarErrorEstado(String mensaje) {
        estadoProcesoLabel.setText(mensaje);
        estadoProcesoLabel.getStyleClass().removeAll("status-ok");
        estadoProcesoLabel.getStyleClass().add("status-error");
    }

    private void limpiarEstilosEstadoProceso() {
        estadoProcesoLabel.getStyleClass().removeAll("status-ok", "status-error");
    }

    private void agregarEpsilon() {
        String actual = palabrasArea.getText();
        if (actual == null || actual.isBlank()) {
            palabrasArea.setText("ε");
        } else if (actual.endsWith("\n") || actual.endsWith("\r")) {
            palabrasArea.appendText("ε");
        } else {
            palabrasArea.appendText(System.lineSeparator() + "ε");
        }
        palabrasArea.requestFocus();
        palabrasArea.positionCaret(palabrasArea.getText().length());
    }

    /**
     * Marca un paso como completado en el stepper visual.
     * 
     * Actualiza las clases CSS del step indicado y activa el siguiente.
     */
    private void marcarPasoCompleto(int paso) {
        if (!esPasoValido(paso)) {
            return;
        }
        marcarPasoActual(paso);
        activarPasoSiguiente(paso);
    }

    private boolean esPasoValido(int paso) {
        return stepBoxes != null && paso >= 1 && paso <= stepBoxes.length && stepBoxes[paso - 1] != null;
    }

    private void marcarPasoActual(int paso) {
        VBox stepBox = stepBoxes[paso - 1];
        stepBox.getStyleClass().add("completed");
        if (!(stepBox.getChildren().get(0) instanceof Label num)) {
            return;
        }
        num.getStyleClass().removeAll("active");
        num.getStyleClass().add("completed-number");
    }

    private void activarPasoSiguiente(int paso) {
        if (paso >= stepBoxes.length || stepBoxes[paso] == null) {
            return;
        }
        VBox nextStep = stepBoxes[paso];
        nextStep.getStyleClass().add("active");
        if (nextStep.getChildren().get(0) instanceof Label nextNum) {
            nextNum.getStyleClass().add("active");
        }
    }

    //Callback ejecutado cuando se carga un autómata desde archivo.
    private void onAutomataLoaded() {
        var automata = controller.getAutomataActual();
        tipoCombo.setValue(automata.getTipo());
        alfabetoField.setText(String.join(",", automata.getAlfabeto()));
        modoCrearEstado = false;
        estadoSeleccionadoCanvas = null;
        simulationManager.detenerSimulacion();
        simulationManager.limpiarResultados();
        uiBuilder.mostrarPanelPruebas(panelConfiguracion, panelPruebas);
        redibujar();
    }

    //Callback ejecutado cuando se reinicia la aplicación o se carga un nuevo autómata.
    private void onAutomataReset() {
        modoCrearEstado = false;
        tipoCombo.setValue(TipoAutomata.DFA);
        alfabetoField.clear();
        palabrasArea.clear();
        resultadosLoteList.getItems().clear();
        estadoSeleccionadoCanvas = null;
        simulationManager.detenerSimulacion();
        btnSiguientePaso.setDisable(true);
        btnReproducir.setDisable(true);
        btnVerFuncion.setDisable(true);
        uiBuilder.mostrarPanelConfiguracion(panelConfiguracion, panelPruebas);
        reiniciarStepperVisual();
        redibujar();
    }

    private void reiniciarStepperVisual() {
        for (VBox stepBox : stepBoxes) {
            if (stepBox != null) {
                stepBox.getStyleClass().removeAll("completed", "active");
            }
        }
        if (stepBoxes[0] != null && stepBoxes[0].getChildren().size() > 0) {
            Label num = (Label) stepBoxes[0].getChildren().get(0);
            num.getStyleClass().add("active");
        }
    }
}
