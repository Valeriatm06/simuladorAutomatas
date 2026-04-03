package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import co.edu.uptc.simuladorautomatas.logic.EvaluacionCadenaResultado;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AutomataView - Vista principal del simulador de autómatas.
 * 
 * Esta clase coordina los componentes de la UI utilizando clases especializadas:
 * - AutomataViewUIBuilder: Construcción de paneles UI
 * - AutomataViewDrawing: Dibujado en canvas
 * - AutomataViewInteraction: Interacción con estados/transiciones
 * - AutomataViewSimulation: Simulación y evaluación
 * - AutomataViewFileOps: Operaciones de archivo
 */
public class AutomataView {
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
    private ComboBox<TipoAutomata> tipoCombo;
    private VBox panelConfiguracion;
    private VBox panelPruebas;
    private VBox[] stepBoxes = new VBox[4];

    // Clases de soporte
    private AutomataViewUIBuilder uiBuilder;
    private AutomataViewDrawing drawingEngine;
    private AutomataViewInteraction interactionHandler;
    private AutomataViewSimulation simulationManager;
    private AutomataViewFileOps fileOps;

    // Estados de UI
    private String estadoSeleccionadoCanvas;
    private String estadoArrastrandose;
    private double ultimaXMouse;
    private double ultimaYMouse;
    private boolean modoCrearEstado;
    private String estadoEnCreacion;
    private boolean nuevoEstadoInicial;
    private boolean nuevoEstadoAceptacion;

    public AutomataView(Stage stage) {
        this.stage = stage;
        this.controller = new AutomataController();
    }

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

        // Toolbar izquierda
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

        // Canvas central
        panelDibujo = new Pane();
        panelDibujo.setPrefSize(900, 620);
        panelDibujo.getStyleClass().add("canvas-pane");
        panelDibujo.setOnMouseClicked(e -> onCanvasMouseClicked(e.getX(), e.getY()));
        panelDibujo.setOnMousePressed(e -> onCanvasMousePressed(e.getX(), e.getY()));
        panelDibujo.setOnMouseDragged(e -> onCanvasMouseDragged(e.getX(), e.getY()));
        panelDibujo.setOnMouseReleased(e -> onCanvasMouseReleased());
        panelDibujo.widthProperty().addListener((o, ol, nw) -> redibujar());
        panelDibujo.heightProperty().addListener((o, ol, nw) -> redibujar());

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), panelDibujo);
        fadeIn.setFromValue(0.8);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        contenedor.setCenter(panelDibujo);

        // Panel detalles derecha
        inicializarComponentesUI();
        VBox[] panelConfigOut = new VBox[1];
        VBox[] panelPruebasOut = new VBox[1];
        
        StackPane panelDetalles = uiBuilder.crearPanelDetalles(
            tipoCombo, alfabetoField, this::crearNuevoAutomata,
            palabrasArea, this::evaluarPalabras, 
            () -> palabrasArea.clear(),
            this::agregarEpsilon,
            resultadosLoteList, btnSiguientePaso, btnReproducir,
            this::avanzarSimulacionManual,
            this::reproducirDesdeInicio,
            estadoProcesoLabel,
            panelConfigOut, panelPruebasOut
        );
        
        panelConfiguracion = panelConfigOut[0];
        panelPruebas = panelPruebasOut[0];
        
        contenedor.setRight(panelDetalles);
        BorderPane.setMargin(panelDetalles, new Insets(0, 0, 0, 8));

        return contenedor;
    }

    private void inicializarComponentesUI() {
        tipoCombo = new ComboBox<>();
        alfabetoField = new TextField();
        palabrasArea = new TextArea();
        resultadosLoteList = new ListView<>();
        resultadosLoteList.setCellFactory(lv -> new ListCell<EvaluacionCadenaResultado>() {
            @Override
            protected void updateItem(EvaluacionCadenaResultado item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String valorCadena = item.getCadena().isEmpty() ? "ε" : item.getCadena();
                    setText((getIndex() + 1) + ". " + valorCadena + " -> " + item.getEstadoTexto());
                }
            }
        });
        resultadosLoteList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                reproducirCadenaSeleccionadaLote();
            }
        });
        btnSiguientePaso = new Button("Siguiente paso");
        btnReproducir = new Button("Reproducir");
        estadoProcesoLabel = new Label("Seleccione una cadena de los resultados");

        // Inicializar clases de soporte
        drawingEngine = new AutomataViewDrawing(panelDibujo);
        interactionHandler = new AutomataViewInteraction(controller, drawingEngine);
        simulationManager = new AutomataViewSimulation(controller);
        fileOps = new AutomataViewFileOps(stage, controller);

        // Callbacks
        interactionHandler.setOnStatusChange(this::redibujar);
        simulationManager.setRedrawCallback(this::redibujar);
        fileOps.setOnAutomataLoaded(this::onAutomataLoaded);
        fileOps.setOnAutomataReset(this::onAutomataReset);

        // Stepper
        uiBuilder.crearStepperVisual(stepBoxes);
    }

    // ======================== EVENT HANDLERS ========================

    private void onCanvasMouseClicked(double x, double y) {
        if (modoCrearEstado) {
            crearEstadoEnCanvas(x, y);
            return;
        }
        String estado = interactionHandler.seleccionarEstadoEnCanvas(x, y);
        if (estado != null) {
            estadoSeleccionadoCanvas = estado;
            mostrarInfoEstado("Estado seleccionado: " + estado);
            redibujar();
        } else {
            // Deseleccionar si se hace click en el vacío
            if (estadoSeleccionadoCanvas != null) {
                estadoSeleccionadoCanvas = null;
                redibujar();
            }
        }
    }

    private void onCanvasMousePressed(double x, double y) {
        if (modoCrearEstado || estadoEnCreacion != null) return;
        
        var estado = interactionHandler.encontrarEstadoEnPosicion(x, y);
        if (estado != null) {
            estadoArrastrandose = estado.getNombre();
            ultimaXMouse = x;
            ultimaYMouse = y;
        }
    }

    private void onCanvasMouseDragged(double x, double y) {
        if (estadoArrastrandose == null) return;
        
        var estado = controller.getAutomataActual().getEstados().stream()
            .filter(e -> e.getNombre().equals(estadoArrastrandose))
            .findFirst();
        
        if (estado.isPresent()) {
            double deltaX = (x - ultimaXMouse) / drawingEngine.getEscala();
            double deltaY = (y - ultimaYMouse) / drawingEngine.getEscala();
            
            double nuevaX = Math.max(46, Math.min(900 - 46, estado.get().getX() + deltaX));
            double nuevaY = Math.max(46, Math.min(620 - 46, estado.get().getY() + deltaY));
            
            estado.get().setX(nuevaX);
            estado.get().setY(nuevaY);
            
            ultimaXMouse = x;
            ultimaYMouse = y;
            redibujar();
        }
    }

    private void onCanvasMouseReleased() {
        if (estadoArrastrandose != null) {
            mostrarInfoEstado("Estado '" + estadoArrastrandose + "' movido");
            estadoArrastrandose = null;
        }
    }

    // ======================== AUTOMATA OPERATIONS ========================

    private void crearNuevoAutomata() {
        try {
            String alfabetoText = alfabetoField.getText().trim();
            if (alfabetoText.isEmpty()) {
                mostrarErrorEstado("Ingrese al menos un símbolo en el alfabeto (ej: a,b)");
                return;
            }
            
            List<String> alfabeto = Arrays.stream(alfabetoText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            if (alfabeto.isEmpty()) {
                mostrarErrorEstado("Ingrese al menos un símbolo en el alfabeto (ej: a,b)");
                return;
            }
            
            controller.nuevoAutomata(tipoCombo.getValue(), alfabeto);
            simulationManager.detenerSimulacion();
            redibujar();
            mostrarInfoEstado("Autómata " + tipoCombo.getValue() + " creado. Agregue estados con el toolbar.");
            uiBuilder.mostrarPanelPruebas(panelConfiguracion, panelPruebas);
            marcarPasoCompleto(1);
        } catch (Exception ex) {
            mostrarErrorEstado("Error: " + ex.getMessage());
        }
    }

    private void activarModoCreacionEstado() {
        interactionHandler.mostrarDialogoNuevoEstado(() -> {
            modoCrearEstado = true;
            mostrarInfoEstado("Click en canvas para ubicar el estado");
        });
    }

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
        try {
            interactionHandler.crearEstadoEnPosicion(
                nombreEstado, 
                interactionHandler.isEstadoEnCreacionInicial(), 
                interactionHandler.isEstadoEnCreacionAceptacion(), 
                x, y);
            modoCrearEstado = false;
            estadoSeleccionadoCanvas = nombreEstado;
            interactionHandler.limpiarEstadoEnCreacion();
            redibujar();
            marcarPasoCompleto(2);
            mostrarInfoEstado("Estado '" + nombreEstado + "' creado");
        } catch (Exception ex) {
            mostrarErrorEstado(ex.getMessage());
        }
    }

    // ======================== EVALUATION ========================

    private void evaluarPalabras() {
        try {
            List<EvaluacionCadenaResultado> resultados = simulationManager.evaluarPalabras(palabrasArea.getText());
            resultadosLoteList.getItems().setAll(resultados);
            
            if (!resultados.isEmpty()) {
                resultadosLoteList.getSelectionModel().select(0);
            }

            long aceptadas = resultados.stream().filter(EvaluacionCadenaResultado::isAceptada).count();
            estadoProcesoLabel.getStyleClass().removeAll("status-ok", "status-error");
            estadoProcesoLabel.getStyleClass().add("status-ok");
            estadoProcesoLabel.setText("Evaluadas " + resultados.size() + " cadenas. " + aceptadas
                    + " aceptadas. Seleccione una para ver paso a paso.");
            marcarPasoCompleto(4);
        } catch (Exception ex) {
            mostrarErrorEstado(ex.getMessage());
        }
    }

    private void reproducirCadenaSeleccionadaLote() {
        EvaluacionCadenaResultado seleccionado = resultadosLoteList.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarErrorEstado("Seleccione una cadena para ver su paso a paso");
            return;
        }

        simulationManager.iniciarSimulacion(seleccionado);
        String valorCadena = seleccionado.getCadena().isEmpty() ? "ε" : seleccionado.getCadena();
        estadoProcesoLabel.getStyleClass().removeAll("status-ok", "status-error");
        estadoProcesoLabel.getStyleClass().add(seleccionado.isAceptada() ? "status-ok" : "status-error");
        estadoProcesoLabel.setText("Cadena: " + valorCadena + " -> " + seleccionado.getEstadoTexto());
        btnSiguientePaso.setDisable(false);
        btnReproducir.setDisable(false);
        redibujar();
    }

    private void avanzarSimulacionManual() {
        simulationManager.avanzarManual();
        redibujar();
    }

    private void reproducirDesdeInicio() {
        simulationManager.reproducirDesdeInicio();
        redibujar();
    }

    // ======================== DRAWING ========================

    private void redibujar() {
        drawingEngine.redibujar(
            controller.getAutomataActual(),
            estadoSeleccionadoCanvas,
            simulationManager.getEstadosResaltados(),
            simulationManager.getEstadosFinales(),
            simulationManager.getUltimoResultado()
        );
    }

    // ======================== UI HELPERS ========================

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

    private void marcarPasoCompleto(int paso) {
        if (stepBoxes != null && paso >= 1 && paso <= stepBoxes.length && stepBoxes[paso - 1] != null) {
            VBox stepBox = stepBoxes[paso - 1];
            stepBox.getStyleClass().add("completed");
            
            if (stepBox.getChildren().size() > 0 && stepBox.getChildren().get(0) instanceof Label) {
                Label num = (Label) stepBox.getChildren().get(0);
                num.getStyleClass().removeAll("active");
                num.getStyleClass().add("completed-number");
            }
            
            if (paso < stepBoxes.length && stepBoxes[paso] != null) {
                VBox nextStep = stepBoxes[paso];
                nextStep.getStyleClass().add("active");
                if (nextStep.getChildren().size() > 0 && nextStep.getChildren().get(0) instanceof Label) {
                    Label nextNum = (Label) nextStep.getChildren().get(0);
                    nextNum.getStyleClass().add("active");
                }
            }
        }
    }

    // ======================== FILE OPERATIONS CALLBACKS ========================

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
