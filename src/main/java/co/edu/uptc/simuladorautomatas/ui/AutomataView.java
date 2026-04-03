package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import co.edu.uptc.simuladorautomatas.logic.EvaluacionCadenaResultado;
import co.edu.uptc.simuladorautomatas.logic.PasoEvaluacion;
import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.SimbolosAutomata;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AutomataView {
    private static final double RADIO_ESTADO = 30;
    private static final double CANVAS_LOGICAL_WIDTH = 900;
    private static final double CANVAS_LOGICAL_HEIGHT = 620;
    private static final double CANVAS_PADDING = 46;

    private final Stage stage;
    private final AutomataController controller;

    private Pane panelDibujo;
    private Label estadoProcesoLabel;
    private Button btnSiguientePaso;
    private Button btnReproducir;

    private ComboBox<TipoAutomata> tipoCombo;
    private TextField alfabetoField;
    private TextArea palabrasArea;
    private ListView<EvaluacionCadenaResultado> resultadosLoteList;
    private Button btnVerPasoLote;
    private VBox panelConfiguracion;
    private VBox panelPruebas;
    
    // Tracking del progreso
    private VBox[] stepBoxes;
    private int pasoActual = 1;
    private boolean paso2Completado = false;
    private boolean paso3Completado = false;
    private boolean paso4Completado = false;

    private boolean modoCrearEstado;
    private boolean nuevoEstadoInicial;
    private boolean nuevoEstadoAceptacion;
    private String estadoSeleccionadoCanvas;
    private int modoActual; // 0=normal, 1=crear estado, 2=crear transicion, 3=seleccionar
    private String estadoEnCreacion; // Nombre del estado siendo creado
    private final List<double[]> zonasEtiquetasTransicion = new ArrayList<>();
    
    // Variables para arrastre de estados
    private String estadoArrastrandose;
    private double ultimaXMouse;
    private double ultimaYMouse;
    private final Set<String> estadosResaltadosEvaluacion = new LinkedHashSet<>();
    private final Set<String> estadosFinalesEvaluacion = new LinkedHashSet<>();
    private EvaluacionCadenaResultado simulacionActual;
    private int indicePasoActual = -1;
    private PauseTransition pausaSimulacion;
    private boolean reproduccionAutomaticaActiva;
    private Boolean ultimoResultadoAceptado;
    private List<EvaluacionCadenaResultado> resultadosUltimoLote = new ArrayList<>();

    public AutomataView(Stage stage) {
        this.stage = stage;
        this.controller = new AutomataController();
    }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root-with-grid");
        root.setTop(crearHeader());
        root.setCenter(crearZonaPrincipal());
        // El autómata se crea cuando el usuario completa el formulario en el panel derecho
        return root;
    }

    private Parent crearHeader() {
        Label titulo = new Label("Simulador y Analizador de Automatas (DFA / NFA)");
        titulo.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button reiniciarBtn = new Button("Reiniciar");
        reiniciarBtn.getStyleClass().add("btn-danger");
        reiniciarBtn.setOnAction(e -> confirmarReinicio());

        HBox filaSuperior = new HBox(10, titulo, spacer, reiniciarBtn);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        Region spacer2 = new Region();
        spacer2.setPrefHeight(20);

        VBox header = new VBox(8, filaSuperior, spacer2);
        header.getStyleClass().add("top-header");
        return header;
    }

    private HBox crearStepperVisual() {
        HBox stepsContainer = new HBox(20);
        stepsContainer.getStyleClass().add("stepper-container");
        stepsContainer.setPadding(new Insets(0, 0, 8, 0));

        String[] steps = {"CREAR AUTOMATA", "DEFINIR ESTADOS", "DEFINIR TRANSICIONES", "EVALUAR CADENAS"};
        stepBoxes = new VBox[steps.length];

        for (int i = 0; i < steps.length; i++) {
            VBox stepBox = new VBox(4);
            stepBox.getStyleClass().add("step-item");
            stepBoxes[i] = stepBox;

            Label stepNum = new Label(String.valueOf(i + 1));
            stepNum.getStyleClass().add("step-number");
            stepNum.setPrefSize(28, 28);
            stepNum.setStyle("-fx-alignment: center;");

            Label stepLabel = new Label(steps[i]);
            stepLabel.getStyleClass().add("step-label");
            stepLabel.setStyle("-fx-font-size: 10px;");

            stepBox.getChildren().addAll(stepNum, stepLabel);

            if (i == 0) {
                stepNum.getStyleClass().add("active");
                stepLabel.getStyleClass().add("active");
            }

            stepsContainer.getChildren().add(stepBox);

            if (i < steps.length - 1) {
                Region line = new Region();
                line.setStyle("-fx-background-color: #E2E8F0; -fx-pref-width: 20; -fx-pref-height: 2;");
                stepsContainer.getChildren().add(line);
            }
        }

        return stepsContainer;
    }

    private Parent crearZonaPrincipal() {
        BorderPane contenedor = new BorderPane();
        contenedor.setPadding(new Insets(12));
        
        // Barra de herramientas flotante izquierda
        VBox toolbarIzquierda = crearToolbarFlotante();
        contenedor.setLeft(toolbarIzquierda);
        BorderPane.setMargin(toolbarIzquierda, new Insets(0, 8, 0, 0));
        
        // Canvas central (protagonista)
        panelDibujo = crearPanelVisual();
        contenedor.setCenter(panelDibujo);

        // Panel de detalles contextual derecha
        javafx.scene.layout.StackPane panelDetalles = crearPanelDetallesContextual();
        contenedor.setRight(panelDetalles);
        BorderPane.setMargin(panelDetalles, new Insets(0, 0, 0, 8));

        return contenedor;
    }

    private VBox crearToolbarFlotante() {
        VBox toolbar = new VBox(6);
        toolbar.getStyleClass().add("floating-toolbar");
        toolbar.setAlignment(Pos.TOP_CENTER);
        toolbar.setPrefWidth(60);
        toolbar.setMaxWidth(60);
        toolbar.setMinWidth(60);

        Button btnEstado = crearBotonToolbar("◯", "Agregar Estado", e -> activarModoCreacionEstado());
        Button btnTransicion = crearBotonToolbar("→", "Agregar Transción", e -> activarModoCreacionTransicion());
        Button btnBorrar = crearBotonToolbar("✕", "Eliminar Seleccionado", e -> eliminarElementoSeleccionado());
        
        // Separador
        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setPadding(new Insets(8, 0, 8, 0));
        sep.setStyle("-fx-padding: 8 0 8 0;");
        
        // Botones de archivo
        Button btnGuardar = crearBotonToolbar("💾", "Guardar Autómata", e -> guardarAutomata());
        Button btnCargar = crearBotonToolbar("📂", "Cargar Autómata", e -> cargarAutomata());
        Button btnAyuda = crearBotonToolbar("❓", "Ayuda", e -> mostrarAyuda());

        toolbar.getChildren().addAll(btnEstado, btnTransicion, btnBorrar, sep, btnGuardar, btnCargar, btnAyuda);
        return toolbar;
    }

    private Button crearBotonToolbar(String texto, String tooltip, EventHandler<ActionEvent> action) {
        Button btn = new Button(texto);
        btn.getStyleClass().add("btn-action");
        btn.setPrefSize(36, 36);
        btn.setOnAction(action);
        btn.setTooltip(new javafx.scene.control.Tooltip(tooltip));
        return btn;
    }

    private javafx.scene.layout.StackPane crearPanelDetallesContextual() {
        javafx.scene.layout.StackPane stackPane = new javafx.scene.layout.StackPane();
        stackPane.setPrefWidth(300);
        stackPane.setMinWidth(250);
        stackPane.setMaxWidth(350);

        // PANEL 1: Configuración del autómata
        panelConfiguracion = new VBox(8);
        panelConfiguracion.getStyleClass().add("details-panel");
        panelConfiguracion.setPrefWidth(300);
        panelConfiguracion.setMinWidth(250);
        panelConfiguracion.setMaxWidth(350);

        Label titulo = new Label("Configuración");
        titulo.getStyleClass().add("section-title");

        VBox seccionDefinicion = new VBox(6);
        Label labelTipo = new Label("Tipo de Autómata:");
        labelTipo.getStyleClass().add("field-label");
        
        tipoCombo = new ComboBox<>();
        tipoCombo.getItems().addAll(TipoAutomata.DFA, TipoAutomata.NFA);
        tipoCombo.setValue(TipoAutomata.DFA);
        tipoCombo.setPrefWidth(Double.MAX_VALUE);

        Label labelAlfabeto = new Label("Alfabeto (a,b,c...):");
        labelAlfabeto.getStyleClass().add("field-label");
        
        alfabetoField = new TextField();
        alfabetoField.setPromptText("Ej: a,b");
        alfabetoField.setPrefWidth(Double.MAX_VALUE);

        Button btnCrearAutomata = new Button("Crear Autómata");
        btnCrearAutomata.getStyleClass().add("btn-primary");
        btnCrearAutomata.setPrefWidth(Double.MAX_VALUE);
        btnCrearAutomata.setOnAction(e -> crearNuevoAutomata());

        seccionDefinicion.getChildren().addAll(
            labelTipo, tipoCombo,
            labelAlfabeto, alfabetoField,
            new Separator(Orientation.HORIZONTAL),
            btnCrearAutomata
        );

        ScrollPane scrollDetalles = new ScrollPane(seccionDefinicion);
        scrollDetalles.setFitToWidth(true);
        VBox.setVgrow(scrollDetalles, Priority.ALWAYS);

        panelConfiguracion.getChildren().addAll(titulo, scrollDetalles);

        // PANEL 2: Prueba de palabras
        panelPruebas = new VBox(8);
        panelPruebas.getStyleClass().add("details-panel");
        panelPruebas.setPrefWidth(300);
        panelPruebas.setMinWidth(250);
        panelPruebas.setMaxWidth(350);
        panelPruebas.setVisible(false);
        panelPruebas.setManaged(false);

        Label tituloPruebas = new Label("Palabras de Prueba");
        tituloPruebas.getStyleClass().add("section-title");

        palabrasArea = new TextArea();
        palabrasArea.setPromptText("Ingrese una palabra por línea" + System.lineSeparator() +
                "Use ε/lambda para palabra vacía" + System.lineSeparator());
        palabrasArea.setWrapText(true);
        palabrasArea.setPrefRowCount(8);

        Button btnEvaluarTodas = new Button("Evaluar Todas");
        btnEvaluarTodas.getStyleClass().add("btn-primary");
        btnEvaluarTodas.setMaxWidth(Double.MAX_VALUE);
        btnEvaluarTodas.setOnAction(e -> evaluarPalabras());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.getStyleClass().add("btn-action");
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> palabrasArea.clear());

        Button btnEpsilonPalabra = new Button("ε");
        btnEpsilonPalabra.getStyleClass().add("btn-secondary");
        btnEpsilonPalabra.setTooltip(new Tooltip("Agregar palabra vacía (epsilon)"));
        btnEpsilonPalabra.setOnAction(e -> {
            String actual = palabrasArea.getText();
            if (actual == null || actual.isBlank()) {
                palabrasArea.setText(SimbolosAutomata.EPSILON);
            } else if (actual.endsWith("\n") || actual.endsWith("\r")) {
                palabrasArea.appendText(SimbolosAutomata.EPSILON);
            } else {
                palabrasArea.appendText(System.lineSeparator() + SimbolosAutomata.EPSILON);
            }
            palabrasArea.requestFocus();
            palabrasArea.positionCaret(palabrasArea.getText().length());
        });

        HBox botonesPruebas = new HBox(8, btnEvaluarTodas, btnLimpiar, btnEpsilonPalabra);
        botonesPruebas.setStyle("-fx-spacing: 8;");
        HBox.setHgrow(btnEvaluarTodas, Priority.ALWAYS);
        HBox.setHgrow(btnLimpiar, Priority.ALWAYS);

        Label resultadosLabel = new Label("Resultados del lote (seleccione una cadena):");
        resultadosLabel.getStyleClass().add("field-label");

        resultadosLoteList = new ListView<>();
        resultadosLoteList.getStyleClass().add("result-list");
        resultadosLoteList.setPrefHeight(230);
        resultadosLoteList.setPlaceholder(new Label("Evalúe hasta 10 cadenas para ver resultados"));
        resultadosLoteList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(EvaluacionCadenaResultado item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                String valorCadena = item.getCadena().isEmpty() ? "ε" : item.getCadena();
                setText((getIndex() + 1) + ". " + valorCadena + " -> " + item.getEstadoTexto());
            }
        });

        // btnVerPasoLote eliminado - usar doble clic en la lista en su lugar

        resultadosLoteList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                reproducirCadenaSeleccionadaLote();
            }
        });

        btnSiguientePaso = new Button("Siguiente paso");
        btnSiguientePaso.getStyleClass().add("btn-secondary");
        btnSiguientePaso.setDisable(true);
        btnSiguientePaso.setMaxWidth(Double.MAX_VALUE);
        btnSiguientePaso.setOnAction(e -> avanzarSimulacionManual());

        btnReproducir = new Button("Reproducir");
        btnReproducir.getStyleClass().add("btn-secondary");
        btnReproducir.setDisable(true);
        btnReproducir.setMaxWidth(Double.MAX_VALUE);
        btnReproducir.setOnAction(e -> reproducirDesdeInicio());

        HBox botonesSimulacion = new HBox(8, btnSiguientePaso, btnReproducir);
        botonesSimulacion.setStyle("-fx-spacing: 8;");
        botonesSimulacion.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnSiguientePaso, Priority.ALWAYS);
        HBox.setHgrow(btnReproducir, Priority.ALWAYS);

        VBox.setVgrow(resultadosLoteList, Priority.ALWAYS);

        estadoProcesoLabel = new Label("Seleccione una cadena de los resultados");
        estadoProcesoLabel.setMaxWidth(Double.MAX_VALUE);
        estadoProcesoLabel.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #64748B; " +
            "-fx-wrap-text: true;"
        );
        estadoProcesoLabel.getStyleClass().add("status-chip");

        panelPruebas.getChildren().addAll(
                tituloPruebas,
                palabrasArea,
                botonesPruebas,
                new Separator(Orientation.HORIZONTAL),
                resultadosLabel,
                resultadosLoteList,
                botonesSimulacion,
                estadoProcesoLabel
        );

        stackPane.getChildren().addAll(panelConfiguracion, panelPruebas);
        return stackPane;
    }

    private void crearNuevoAutomata() {
        try {
            String alfabetoText = alfabetoField.getText().trim();
            if (alfabetoText.isEmpty()) {
                mostrarError("Ingrese al menos un símbolo en el alfabeto (ej: a,b)");
                return;
            }
            
            List<String> alfabeto = Arrays.stream(alfabetoText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            if (alfabeto.isEmpty()) {
                mostrarError("Ingrese al menos un símbolo en el alfabeto (ej: a,b)");
                return;
            }
            
            controller.nuevoAutomata(tipoCombo.getValue(), alfabeto);
            System.out.println("DEBUG: Autómata creado con tipo " + tipoCombo.getValue() + " y alfabeto " + alfabeto);
            detenerSimulacionVisual();
            redibujar();
            mostrarInfoEstado("Autómata " + tipoCombo.getValue() + " creado. Agregue estados con el toolbar.");
            
            // Cambiar al panel de pruebas
            mostrarPanelPruebas();
            palabrasArea.clear();
            limpiarResultadosLote();
            
            // Marcar paso 1 como completado
            marcarPasoCompleto(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("Error al crear autómata: " + ex.getMessage());
        }
    }
    
    private void evaluarPalabras() {
        List<String> entradas = Arrays.stream(palabrasArea.getText().split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SimbolosAutomata::normalizarCadenaEntrada)
                .collect(Collectors.toList());

        if (entradas.isEmpty()) {
            mostrarError("Ingrese palabras para evaluar. Use ε o lambda para palabra vacía");
            return;
        }

        try {
            controller.validarAutomata();
            List<EvaluacionCadenaResultado> resultados = controller.evaluarLoteConTraza(entradas);
            resultadosUltimoLote = new ArrayList<>(resultados);
            resultadosLoteList.getItems().setAll(resultadosUltimoLote);

            if (!resultadosUltimoLote.isEmpty()) {
                resultadosLoteList.getSelectionModel().select(0);
            }

            long aceptadas = resultados.stream().filter(EvaluacionCadenaResultado::isAceptada).count();
            estadoProcesoLabel.getStyleClass().removeAll("status-ok", "status-error");
            estadoProcesoLabel.getStyleClass().add("status-ok");
            estadoProcesoLabel.setText("Evaluadas " + resultados.size() + " cadenas. " + aceptadas
                    + " aceptadas y " + (resultados.size() - aceptadas)
                    + " rechazadas. Seleccione una para ver el paso a paso.");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
            return;
        }
        
        // Marcar paso 4 como completado cuando se evalúan palabras
        if (!paso4Completado) {
            marcarPasoCompleto(4);
            paso4Completado = true;
        }
    }

    private void reproducirCadenaSeleccionadaLote() {
        EvaluacionCadenaResultado seleccionado = resultadosLoteList == null
                ? null
                : resultadosLoteList.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione una cadena evaluada para ver su paso a paso");
            return;
        }

        iniciarSimulacionVisual(seleccionado);
        String valorCadena = seleccionado.getCadena().isEmpty() ? "ε" : seleccionado.getCadena();
        estadoProcesoLabel.getStyleClass().removeAll("status-ok", "status-error");
        estadoProcesoLabel.getStyleClass().add(seleccionado.isAceptada() ? "status-ok" : "status-error");
        estadoProcesoLabel.setText("Cadena seleccionada: " + valorCadena + " -> "
                + seleccionado.getEstadoTexto() + ". Use 'Siguiente paso' o 'Reproducir'.");
    }

    private Pane crearPanelVisual() {
        Pane panel = new Pane();
        panel.setMinWidth(400);
        panel.setMinHeight(300);
        panel.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        panel.getStyleClass().add("canvas-pane");

        panel.setOnMouseClicked(event -> {
            if (modoCrearEstado) {
                crearEstadoDesdeCanvas(event.getX(), event.getY());
                return;
            }
            // Siempre intentar seleccionar al hacer click
            seleccionarEstadoEnCanvas(event.getX(), event.getY());
        });
        
        // Manejador para iniciar arrastre
        panel.setOnMousePressed(event -> {
            iniciarArrastre(event.getX(), event.getY());
        });
        
        // Manejador para arrastrar
        panel.setOnMouseDragged(event -> {
            continuarArrastre(event.getX(), event.getY());
        });
        
        // Manejador para terminar arrastre
        panel.setOnMouseReleased(event -> {
            terminarArrastre();
        });

        // Animación de entrada suave
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), panel);
        fadeIn.setFromValue(0.8);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        panel.widthProperty().addListener((obs, oldVal, newVal) -> redibujar());
        panel.heightProperty().addListener((obs, oldVal, newVal) -> redibujar());
        return panel;
    }

    private void evaluarCadenaUnica(String cadena) {
        cadena = cadena.trim();
        if (cadena.isEmpty()) {
            mostrarError("Ingrese una cadena para evaluar");
            return;
        }
        
        try {
            controller.validarAutomata();
            EvaluacionCadenaResultado resultado = controller.evaluarConTraza(cadena);
            
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Cadena: ").append(cadena).append("\n");
            mensaje.append("Resultado: ").append(resultado.getEstadoTexto());
            
            estadoProcesoLabel.setText(mensaje.toString());
            estadoProcesoLabel.getStyleClass().removeAll("status-error");
            
            if (resultado.isAceptada()) {
                estadoProcesoLabel.getStyleClass().add("status-ok");
            } else {
                estadoProcesoLabel.getStyleClass().add("status-error");
            }

            iniciarSimulacionVisual(resultado);

        } catch (Exception ex) {
            detenerSimulacionVisual();
            estadoProcesoLabel.setText("Error: " + ex.getMessage());
            estadoProcesoLabel.getStyleClass().removeAll("status-ok");
            estadoProcesoLabel.getStyleClass().add("status-error");
        }
    }

    private void iniciarSimulacionVisual(EvaluacionCadenaResultado resultado) {
        detenerSimulacionVisual();
        simulacionActual = resultado;
        indicePasoActual = -1;
        reproduccionAutomaticaActiva = false;
        ultimoResultadoAceptado = null;

        estadosResaltadosEvaluacion.clear();
        estadosFinalesEvaluacion.clear();
        estadosResaltadosEvaluacion.addAll(resultado.getEstadosIniciales());
        redibujar();

        boolean tienePasos = !resultado.getPasos().isEmpty();
        if (btnSiguientePaso != null) {
            btnSiguientePaso.setDisable(!tienePasos);
        }
        if (btnReproducir != null) {
            btnReproducir.setDisable(!tienePasos);
        }
        if (!tienePasos) {
            aplicarResultadoFinalVisual(null);
        }
    }

    private void reproducirDesdeInicio() {
        if (simulacionActual == null) {
            return;
        }
        detenerPausaSimulacion();
        reproduccionAutomaticaActiva = true;
        indicePasoActual = -1;
        estadosResaltadosEvaluacion.clear();
        estadosFinalesEvaluacion.clear();
        ultimoResultadoAceptado = null;
        estadosResaltadosEvaluacion.addAll(simulacionActual.getEstadosIniciales());
        redibujar();
        if (!simulacionActual.getPasos().isEmpty()) {
            programarSiguientePaso();
        }
    }

    private void avanzarSimulacionManual() {
        if (simulacionActual == null) {
            return;
        }
        detenerPausaSimulacion();
        reproduccionAutomaticaActiva = false;
        avanzarPasoVisual();
    }

    private void programarSiguientePaso() {
        if (!reproduccionAutomaticaActiva || simulacionActual == null) {
            return;
        }
        detenerPausaSimulacion();
        pausaSimulacion = new PauseTransition(Duration.millis(800));
        pausaSimulacion.setOnFinished(event -> {
            avanzarPasoVisual();
            if (reproduccionAutomaticaActiva
                    && simulacionActual != null
                    && indicePasoActual < simulacionActual.getPasos().size() - 1) {
                programarSiguientePaso();
            }
        });
        pausaSimulacion.play();
    }

    private void detenerPausaSimulacion() {
        if (pausaSimulacion != null) {
            pausaSimulacion.stop();
            pausaSimulacion = null;
        }
    }

    private void avanzarPasoVisual() {
        if (simulacionActual == null) {
            return;
        }
        if (indicePasoActual >= simulacionActual.getPasos().size() - 1) {
            reproduccionAutomaticaActiva = false;
            if (btnSiguientePaso != null) {
                btnSiguientePaso.setDisable(true);
            }
            return;
        }

        indicePasoActual++;
        PasoEvaluacion paso = simulacionActual.getPasos().get(indicePasoActual);

        estadosResaltadosEvaluacion.clear();
        estadosResaltadosEvaluacion.addAll(paso.getEstadosDestino());
        redibujar();

        if (indicePasoActual >= simulacionActual.getPasos().size() - 1 && btnSiguientePaso != null) {
            reproduccionAutomaticaActiva = false;
            aplicarResultadoFinalVisual(paso);
            btnSiguientePaso.setDisable(true);
        }
    }

    private void aplicarResultadoFinalVisual(PasoEvaluacion ultimoPaso) {
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
        redibujar();
    }

    private void detenerSimulacionVisual() {
        detenerPausaSimulacion();
        reproduccionAutomaticaActiva = false;
        simulacionActual = null;
        indicePasoActual = -1;
        estadosResaltadosEvaluacion.clear();
        estadosFinalesEvaluacion.clear();
        ultimoResultadoAceptado = null;
        if (btnSiguientePaso != null) {
            btnSiguientePaso.setDisable(true);
        }
        if (btnReproducir != null) {
            btnReproducir.setDisable(true);
        }
        redibujar();
    }

    private void activarModoCreacionEstado() {
        mostrarDialogoNuevoEstado();
    }

    private void mostrarDialogoNuevoEstado() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Estado");
        dialog.setHeaderText("¿Cuál es el nombre del estado?");

        // Layout principal
        VBox content = new VBox(12);
        content.setPadding(new Insets(12));

        // Campo de nombre
        HBox nombreBox = new HBox(8);
        Label labelNombre = new Label("Nombre:");
        labelNombre.setPrefWidth(80);
        TextField nombreField = new TextField();
        nombreField.setPromptText("Ej: q0");
        nombreField.setPrefWidth(250);
        nombreBox.getChildren().addAll(labelNombre, nombreField);

        // Checkboxes
        CheckBox inicialCheckBox = new CheckBox("Estado Inicial");
        inicialCheckBox.setStyle("-fx-font-size: 12px;");
        
        CheckBox aceptacionCheckBox = new CheckBox("Estado de Aceptación (Final)");
        aceptacionCheckBox.setStyle("-fx-font-size: 12px;");

        content.getChildren().addAll(nombreBox, inicialCheckBox, aceptacionCheckBox);
        dialog.getDialogPane().setContent(content);

        // Botones
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Validación al hacer click en OK
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) {
                    mostrarError("El nombre del estado no puede estar vacío");
                    return null;
                }
                estadoEnCreacion = nombre;
                nuevoEstadoInicial = inicialCheckBox.isSelected();
                nuevoEstadoAceptacion = aceptacionCheckBox.isSelected();
                return nombre;
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nombre -> {
            if (!nombre.isEmpty()) {
                modoCrearEstado = true;
                modoActual = 1;
                mostrarInfoEstado("Click en canvas para ubicar el estado '" + estadoEnCreacion + "'");
            }
        });
    }

    private void activarModoCreacionTransicion() {
        mostrarDialogoNuevaTransicion();
    }

    private void mostrarDialogoNuevaTransicion() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Nueva Transición");
        dialog.setHeaderText("Seleccione estados para la transición");

        VBox content = new VBox(8);
        content.setPadding(new Insets(8));

        Label labelOrigen = new Label("Estado de origen:");
        labelOrigen.getStyleClass().add("field-label");
        ComboBox<String> comboOrigen = new ComboBox<>();
        comboOrigen.getItems().addAll(controller.nombresEstados());

        Label labelSimbolo = new Label("Símbolo:");
        labelSimbolo.getStyleClass().add("field-label");
        TextField campoSimbolo = new TextField();
        campoSimbolo.setPromptText("ej: a (vacío = ε en NFA)");
        Button btnEpsilon = new Button("ε");
        btnEpsilon.getStyleClass().add("btn-secondary");
        btnEpsilon.setOnAction(e -> campoSimbolo.setText(SimbolosAutomata.EPSILON));
        btnEpsilon.setTooltip(new Tooltip("Usar transición epsilon/lambda"));
        HBox simboloBox = new HBox(8, campoSimbolo, btnEpsilon);
        HBox.setHgrow(campoSimbolo, Priority.ALWAYS);

        Label labelDestino = new Label("Estado destino:");
        labelDestino.getStyleClass().add("field-label");
        ComboBox<String> comboDestino = new ComboBox<>();
        comboDestino.getItems().addAll(controller.nombresEstados());

        content.getChildren().addAll(labelOrigen, comboOrigen, labelSimbolo, simboloBox,
                                    labelDestino, comboDestino);

        dialog.getDialogPane().setContent(content);
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String simboloIngresado = campoSimbolo.getText() == null ? "" : campoSimbolo.getText().trim();
                if (simboloIngresado.isEmpty()) {
                    simboloIngresado = SimbolosAutomata.EPSILON;
                }

                controller.agregarTransicion(comboOrigen.getValue(), simboloIngresado,
                                            comboDestino.getValue());
                redibujar();
                mostrarInfoEstado("Transición agregada");
                
                // Marcar paso 3 como completado cuando se crea la primera transición
                if (!paso3Completado) {
                    marcarPasoCompleto(3);
                    paso3Completado = true;
                }
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    private void eliminarElementoSeleccionado() {
        if (estadoSeleccionadoCanvas == null) {
            mostrarError("Seleccione un estado para eliminar");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Estado");
        confirm.setHeaderText("¿Desea eliminar el estado '" + estadoSeleccionadoCanvas + "'?");
        confirm.setContentText("Se eliminarán todas sus transiciones asociadas.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                controller.eliminarEstado(estadoSeleccionadoCanvas);
                estadoSeleccionadoCanvas = null;
                actualizarCombos();
                redibujar();
                mostrarInfoEstado("Estado eliminado");
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    private void crearEstadoDesdeCanvas(double x, double y) {
        if (estadoEnCreacion == null || estadoEnCreacion.isEmpty()) {
            mostrarError("Ingrese el nombre del estado antes de hacer click en el canvas");
            return;
        }
        try {
            controller.agregarEstado(
                    estadoEnCreacion,
                    nuevoEstadoInicial,
                    nuevoEstadoAceptacion,
                    ajustarXLogico(vistaALogicoX(x)),
                    ajustarYLogico(vistaALogicoY(y))
            );
            modoCrearEstado = false;
            estadoSeleccionadoCanvas = estadoEnCreacion;
            estadoEnCreacion = null;
            actualizarCombos();
            redibujar();
            mostrarInfoEstado("Estado creado");
            animarCanvasEntrada();
            
            // Marcar paso 2 como completado cuando se crea el primer estado
            if (!paso2Completado) {
                marcarPasoCompleto(2);
                paso2Completado = true;
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void seleccionarEstadoEnCanvas(double x, double y) {
        Automata automata = controller.getAutomataActual();
        String encontrado = null;
        double mejor = Double.MAX_VALUE;
        for (Estado estado : automata.getEstados()) {
            double ex = logicoAVistaX(estado.getX());
            double ey = logicoAVistaY(estado.getY());
            double d = Math.hypot(ex - x, ey - y);
            if (d <= radioEscalado() + 8 && d < mejor) {
                encontrado = estado.getNombre();
                mejor = d;
            }
        }
        if (encontrado != null) {
            estadoSeleccionadoCanvas = encontrado;
            redibujar();
            mostrarInfoEstado("Estado seleccionado: " + encontrado);
        }
    }
    
    private void iniciarArrastre(double x, double y) {
        if (modoCrearEstado || estadoEnCreacion != null) {
            return; // No permitir arrastre en modo creación
        }
        
        Automata automata = controller.getAutomataActual();
        String encontrado = null;
        double mejor = Double.MAX_VALUE;
        
        // Buscar qué estado está debajo del cursor
        for (Estado estado : automata.getEstados()) {
            double ex = logicoAVistaX(estado.getX());
            double ey = logicoAVistaY(estado.getY());
            double d = Math.hypot(ex - x, ey - y);
            if (d <= radioEscalado() + 8 && d < mejor) {
                encontrado = estado.getNombre();
                mejor = d;
            }
        }
        
        // Si encontramos un estado para arrastrar
        if (encontrado != null) {
            estadoArrastrandose = encontrado;
            ultimaXMouse = x;
            ultimaYMouse = y;
        }
    }
    
    private void continuarArrastre(double x, double y) {
        if (estadoArrastrandose == null) {
            return; // No hay estado siendo arrastrado
        }
        
        Automata automata = controller.getAutomataActual();
        Optional<Estado> estadoOpt = automata.getEstados().stream()
                .filter(e -> e.getNombre().equals(estadoArrastrandose))
                .findFirst();
        
        if (estadoOpt.isPresent()) {
            Estado estado = estadoOpt.get();
            
            // Calcular el desplazamiento en coordenadas lógicas
            double deltaX = (x - ultimaXMouse) / escalaCanvas();
            double deltaY = (y - ultimaYMouse) / escalaCanvas();
            
            // Actualizar posición del estado con límites de canvas
            double nuevaX = ajustarXLogico(estado.getX() + deltaX);
            double nuevaY = ajustarYLogico(estado.getY() + deltaY);
            
            estado.setX(nuevaX);
            estado.setY(nuevaY);
            
            // Actualizar últimas coordenadas del mouse
            ultimaXMouse = x;
            ultimaYMouse = y;
            
            // Redibujar el canvas para mostrar el movimiento en tiempo real
            redibujar();
        }
    }
    
    private void terminarArrastre() {
        if (estadoArrastrandose != null) {
            mostrarInfoEstado("Estado '" + estadoArrastrandose + "' movido");
            estadoArrastrandose = null;
        }
    }

    private void actualizarCombos() {
        // Este método ya no es necesario en el nuevo diseño
        // Las transiciones se manejan vía diálogos
    }

    private void evaluarCadenas() {
        // Este método se reemplazó por evaluarCadenaUnica en la timeline
        mostrarInfoEstado("Use el campo Evaluation Timeline en la parte inferior para evaluar cadenas");
    }

    private void generarTraza() {
        // Este método se reemplazó por evaluarCadenaUnica en la timeline
        mostrarInfoEstado("Use el campo Evaluation Timeline para simular paso a paso");
    }

    private void guardarAutomata() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar automata");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        try {
            controller.validarAutomata();
            controller.guardar(file.toPath());
            mostrarInfoEstado("Automata guardado en " + file.getName());
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void cargarAutomata() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Cargar automata");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        try {
            controller.cargar(file.toPath());
            Automata automata = controller.getAutomataActual();
            tipoCombo.setValue(automata.getTipo());
            alfabetoField.setText(String.join(",", automata.getAlfabeto()));
            modoCrearEstado = false;
            estadoSeleccionadoCanvas = null;
            detenerSimulacionVisual();
            limpiarResultadosLote();
            mostrarPanelPruebas();
            redibujar();
            mostrarInfoEstado("Automata cargado: " + file.getName());
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void mostrarAyuda() {
        Alert ayuda = new Alert(Alert.AlertType.INFORMATION);
        ayuda.setTitle("Ayuda - Simulador de Autómatas");
        ayuda.setHeaderText("Manual de Uso");
        ayuda.setContentText(
            "SIMULADOR DE AUTÓMATAS FINITOS\n\n" +
            "1. CREAR AUTÓMATA:\n" +
            "   • Seleccione el tipo (DFA o NFA)\n" +
            "   • Defina el alfabeto separado por comas\n" +
            "   • Presione 'Crear Autómata'\n\n" +
            "2. AGREGAR ESTADOS:\n" +
            "   • Presione el botón ⊕ en la barra izquierda\n" +
            "   • Ingrese el nombre del estado\n" +
            "   • Seleccione si es inicial o final\n" +
            "   • Haga click en el canvas para ubicarlo\n\n" +
            "3. AGREGAR TRANSICIONES:\n" +
            "   • Presione el botón → en la barra izquierda\n" +
            "   • Seleccione estado origen y destino\n" +
            "   • Ingrese el símbolo del alfabeto (en NFA también puede usar ε/lambda)\n\n" +
            "4. ELIMINAR ELEMENTOS:\n" +
            "   • Presione el botón ✕ para eliminar un estado\n" +
            "   • Seleccione el estado a eliminar\n\n" +
            "5. EVALUAR CADENAS:\n" +
            "   • Use el panel derecho para ingresar palabras\n" +
            "   • Presione 'Evaluar Todas' para probar múltiples cadenas\n\n" +
            "6. GUARDAR Y CARGAR:\n" +
            "   • Use los botones en la barra inferior\n" +
            "   • Los archivos se guardan en formato JSON"
        );
        ayuda.showAndWait();
    }

    private void redibujar() {
        panelDibujo.getChildren().clear();
        zonasEtiquetasTransicion.clear();
        Automata automata = controller.getAutomataActual();

        for (Transicion transicion : automata.getTransiciones()) {
            dibujarTransicion(transicion);
        }
        for (Estado estado : automata.getEstados()) {
            dibujarEstado(estado);
        }
    }

    private void dibujarEstado(Estado estado) {
        boolean seleccionado = estado.getNombre().equals(estadoSeleccionadoCanvas) && modoActual == 3;
        boolean resaltadoEvaluacion = estadosResaltadosEvaluacion.contains(estado.getNombre());
        boolean estadoFinal = estadosFinalesEvaluacion.contains(estado.getNombre());
        double x = logicoAVistaX(estado.getX());
        double y = logicoAVistaY(estado.getY());
        double radio = radioEscalado();

        // Halo brillante si está seleccionado
        if (seleccionado) {
            Circle halo = new Circle(x, y, radio + 12, Color.TRANSPARENT);
            halo.setStroke(Color.web("#2563EB"));
            halo.setStrokeWidth(2.0);
            halo.setStyle("-fx-stroke-dash-array: 4 2;");
            panelDibujo.getChildren().add(halo);

            // Glow effect
            Circle brillo = new Circle(x, y, radio + 6, Color.TRANSPARENT);
            brillo.setStroke(Color.web("rgba(37, 99, 235, 0.3)"));
            brillo.setStrokeWidth(3.0);
            panelDibujo.getChildren().add(brillo);
        }

        if (resaltadoEvaluacion) {
            Circle haloEvaluacion = new Circle(x, y, radio + 9, Color.TRANSPARENT);
            haloEvaluacion.setStroke(Color.web("#F59E0B"));
            haloEvaluacion.setStrokeWidth(2.4);
            panelDibujo.getChildren().add(haloEvaluacion);
        }

        if (estadoFinal && ultimoResultadoAceptado != null) {
            Circle haloFinal = new Circle(x, y, radio + 10, Color.TRANSPARENT);
            haloFinal.setStroke(ultimoResultadoAceptado ? Color.web("#16A34A") : Color.web("#DC2626"));
            haloFinal.setStrokeWidth(2.8);
            panelDibujo.getChildren().add(haloFinal);
        }

        Color colorBase = resaltadoEvaluacion ? Color.web("#FEF3C7") : Color.web("#F8FAFC");
        if (estadoFinal && ultimoResultadoAceptado != null) {
            colorBase = ultimoResultadoAceptado ? Color.web("#DCFCE7") : Color.web("#FEE2E2");
        }
        Circle principal = new Circle(x, y, radio, colorBase);
        principal.setStroke(seleccionado ? Color.web("#2563EB") : Color.web("#1F2937"));
        principal.setStrokeWidth(seleccionado ? 3.0 : 1.7);

        if (estado.isEsInicial() && !estadoFinal) {
            principal.setFill(resaltadoEvaluacion ? Color.web("#FDE68A") : Color.web("#E0F2FE"));
        }

        panelDibujo.getChildren().add(principal);

        if (estado.isEsAceptacion()) {
            Circle interno = new Circle(x, y, radio - 6, Color.TRANSPARENT);
            interno.setStroke(seleccionado ? Color.web("#2563EB") : Color.web("#059669"));
            interno.setStrokeWidth(1.8);
            panelDibujo.getChildren().add(interno);
        }

        if (estado.isEsInicial()) {
            Line flecha = new Line(x - (radio + 32), y, x - radio, y);
            flecha.setStroke(Color.web("#0F172A"));
            Polygon punta = new Polygon(
                    x - radio, y,
                    x - radio - 11, y - 6,
                    x - radio - 11, y + 6
            );
            punta.setFill(Color.web("#0F172A"));
            panelDibujo.getChildren().addAll(flecha, punta);
        }

        Text texto = new Text(estado.getNombre());
        texto.setFont(Font.font(14));
        texto.setFill(Color.web("#0F172A"));
        texto.setX(x - (texto.getLayoutBounds().getWidth() / 2));
        texto.setY(y + 4);
        panelDibujo.getChildren().add(texto);
    }

    private void dibujarTransicion(Transicion transicion) {
        Estado origen = transicion.getEstadoOrigen();
        Estado destino = transicion.getEstadoDestino();
        double radio = radioEscalado();
        double ox = logicoAVistaX(origen.getX());
        double oy = logicoAVistaY(origen.getY());
        double dx = logicoAVistaX(destino.getX());
        double dy = logicoAVistaY(destino.getY());

        if (origen.equals(destino)) {
            Circle loop = new Circle(ox, oy - radio - 18, 16, Color.TRANSPARENT);
            loop.setStroke(Color.web("#334155"));
            loop.setStrokeWidth(1.6);
            Text etiqueta = new Text(formatearSimboloVisual(transicion.getSimbolo()));
            etiqueta.setFont(Font.font(13));
            double textoW = etiqueta.getLayoutBounds().getWidth();
            double textoH = etiqueta.getLayoutBounds().getHeight();
            double[] pos = ajustarPosicionEtiqueta(
                    ox,
                    oy - radio - 38,
                    textoW,
                    textoH,
                    0,
                    -1
            );
            StackPane chip = crearChipEtiqueta(etiqueta, pos[0], pos[1]);
            panelDibujo.getChildren().addAll(loop, chip);
            return;
        }

        double deltaX = dx - ox;
        double deltaY = dy - oy;
        double longitud = Math.hypot(deltaX, deltaY);
        if (longitud == 0) {
            return;
        }

        double ux = deltaX / longitud;
        double uy = deltaY / longitud;
        double px = -uy;
        double py = ux;

        double inicioX = ox + ux * radio;
        double inicioY = oy + uy * radio;
        double finX = dx - ux * radio;
        double finY = dy - uy * radio;

        double curvatura = 18 + Math.min(26, longitud * 0.07);
        double controlX = (inicioX + finX) / 2 + px * curvatura;
        double controlY = (inicioY + finY) / 2 + py * curvatura;

        QuadCurve curva = new QuadCurve(inicioX, inicioY, controlX, controlY, finX, finY);
        curva.setFill(Color.TRANSPARENT);
        curva.setStroke(Color.web("#334155"));
        curva.setStrokeWidth(1.7);

        double tx = finX - controlX;
        double ty = finY - controlY;
        double tLen = Math.hypot(tx, ty);
        if (tLen == 0) {
            return;
        }
        double tux = tx / tLen;
        double tuy = ty / tLen;

        double arrowSize = 8;
        Polygon punta = new Polygon(
                finX, finY,
                finX - tux * 14 - tuy * arrowSize, finY - tuy * 14 + tux * arrowSize,
                finX - tux * 14 + tuy * arrowSize, finY - tuy * 14 - tux * arrowSize
        );
        punta.setFill(Color.web("#334155"));

        Text etiqueta = new Text(formatearSimboloVisual(transicion.getSimbolo()));
        etiqueta.setFont(Font.font(13));
        double textoW = etiqueta.getLayoutBounds().getWidth();
        double textoH = etiqueta.getLayoutBounds().getHeight();

        // Punto medio de la curva (t=0.5) y offset normal para separar la etiqueta de la línea.
        double medioX = (inicioX + (2 * controlX) + finX) / 4.0;
        double medioY = (inicioY + (2 * controlY) + finY) / 4.0;
        double etiquetaOffset = Math.max(14.0, radio * 0.45);
        double baseEtiquetaX = medioX + (px * etiquetaOffset);
        double baseEtiquetaY = medioY + (py * etiquetaOffset);
        double[] pos = ajustarPosicionEtiqueta(baseEtiquetaX, baseEtiquetaY, textoW, textoH, px, py);
        StackPane chip = crearChipEtiqueta(etiqueta, pos[0], pos[1]);

        panelDibujo.getChildren().addAll(curva, punta, chip);
    }

    private StackPane crearChipEtiqueta(Text etiquetaTexto, double centerX, double centerY) {
        etiquetaTexto.setFill(Color.web("#1D4ED8"));
        double textoW = etiquetaTexto.getLayoutBounds().getWidth();
        double textoH = etiquetaTexto.getLayoutBounds().getHeight();

        Rectangle fondo = new Rectangle(textoW + 10, textoH + 6);
        fondo.setArcWidth(10);
        fondo.setArcHeight(10);
        fondo.setFill(Color.web("rgba(248, 250, 252, 0.95)"));
        fondo.setStroke(Color.web("#CBD5E1"));
        fondo.setStrokeWidth(0.7);

        StackPane chip = new StackPane(fondo, etiquetaTexto);
        chip.setLayoutX(centerX - ((textoW + 10) / 2));
        chip.setLayoutY(centerY - ((textoH + 6) / 2));
        return chip;
    }

    private double[] ajustarPosicionEtiqueta(double centerX, double centerY, double textoW, double textoH,
                                             double normalX, double normalY) {
        double x = centerX;
        double y = centerY;
        double ancho = textoW + 10;
        double alto = textoH + 6;

        for (int intento = 0; intento < 7; intento++) {
            double left = x - (ancho / 2.0);
            double top = y - (alto / 2.0);
            double right = left + ancho;
            double bottom = top + alto;

            if (!colisionaEtiqueta(left, top, right, bottom)) {
                zonasEtiquetasTransicion.add(new double[] {left, top, right, bottom});
                return new double[] {x, y};
            }

            double signo = (intento % 2 == 0) ? 1.0 : -1.0;
            double paso = 10 + (intento * 5);
            x += normalX * paso * signo;
            y += normalY * paso * signo;
        }

        double left = x - (ancho / 2.0);
        double top = y - (alto / 2.0);
        zonasEtiquetasTransicion.add(new double[] {left, top, left + ancho, top + alto});
        return new double[] {x, y};
    }

    private boolean colisionaEtiqueta(double left, double top, double right, double bottom) {
        for (double[] zona : zonasEtiquetasTransicion) {
            boolean separada = right < zona[0] || left > zona[2] || bottom < zona[1] || top > zona[3];
            if (!separada) {
                return true;
            }
        }
        return false;
    }

    private double escalaCanvas() {
        if (panelDibujo.getWidth() <= 0 || panelDibujo.getHeight() <= 0) {
            return 1.0;
        }
        double sx = panelDibujo.getWidth() / CANVAS_LOGICAL_WIDTH;
        double sy = panelDibujo.getHeight() / CANVAS_LOGICAL_HEIGHT;
        return Math.max(0.72, Math.min(sx, sy));
    }

    private double radioEscalado() {
        return RADIO_ESTADO * escalaCanvas();
    }

    private double logicoAVistaX(double x) {
        return x * escalaCanvas();
    }

    private double logicoAVistaY(double y) {
        return y * escalaCanvas();
    }

    private double vistaALogicoX(double x) {
        return x / escalaCanvas();
    }

    private double vistaALogicoY(double y) {
        return y / escalaCanvas();
    }

    private double ajustarXLogico(double x) {
        return Math.max(CANVAS_PADDING, Math.min(CANVAS_LOGICAL_WIDTH - CANVAS_PADDING, x));
    }

    private double ajustarYLogico(double y) {
        return Math.max(CANVAS_PADDING, Math.min(CANVAS_LOGICAL_HEIGHT - CANVAS_PADDING, y));
    }

    private void resaltarResultados() {
        // Animación opcional for feedback visual
        FadeTransition ft = new FadeTransition(Duration.millis(240), panelDibujo);
        ft.setFromValue(0.95);
        ft.setToValue(1.0);
        ft.play();
    }

    private void animarCanvasEntrada() {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), panelDibujo);
        st.setFromX(0.985);
        st.setFromY(0.985);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    private void confirmarReinicio() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reiniciar automata");
        confirm.setHeaderText("Se eliminaran estados, transiciones y entradas");
        confirm.setContentText("Desea continuar?");
        Optional<ButtonType> respuesta = confirm.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            limpiarVistaYAutomata();
        }
    }

    private void limpiarVistaYAutomata() {
        modoCrearEstado = false;
        modoActual = 0;
        nuevoEstadoInicial = false;
        nuevoEstadoAceptacion = false;
        estadoSeleccionadoCanvas = null;
        estadoEnCreacion = null;
        detenerPausaSimulacion();
        simulacionActual = null;
        indicePasoActual = -1;
        estadosResaltadosEvaluacion.clear();
        estadosFinalesEvaluacion.clear();
        ultimoResultadoAceptado = null;
        alfabetoField.clear();
        palabrasArea.clear();
        limpiarResultadosLote();
        tipoCombo.setValue(TipoAutomata.DFA);
        controller.reiniciarAutomata();
        if (btnSiguientePaso != null) {
            btnSiguientePaso.setDisable(true);
        }
        if (btnReproducir != null) {
            btnReproducir.setDisable(true);
        }
        mostrarPanelConfiguracion();
        reiniciarStepperVisual();
        redibujar();
        mostrarInfoEstado("Lienzo limpio. Defina un nuevo automata");
    }

    private void mostrarPanelConfiguracion() {
        panelConfiguracion.setVisible(true);
        panelConfiguracion.setManaged(true);
        panelPruebas.setVisible(false);
        panelPruebas.setManaged(false);
    }

    private void mostrarPanelPruebas() {
        panelConfiguracion.setVisible(false);
        panelConfiguracion.setManaged(false);
        panelPruebas.setVisible(true);
        panelPruebas.setManaged(true);
    }

    private void limpiarResultadosLote() {
        resultadosUltimoLote.clear();
        if (resultadosLoteList != null) {
            resultadosLoteList.getItems().clear();
        }
        if (btnVerPasoLote != null) {
            btnVerPasoLote.setDisable(true);
        }
    }

    private void reiniciarStepperVisual() {
        pasoActual = 1;
        paso2Completado = false;
        paso3Completado = false;
        paso4Completado = false;
        if (stepBoxes == null) {
            return;
        }
        for (int i = 0; i < stepBoxes.length; i++) {
            VBox stepBox = stepBoxes[i];
            stepBox.getStyleClass().removeAll("completed", "active");
            if (stepBox.getChildren().size() >= 2
                    && stepBox.getChildren().get(0) instanceof Label
                    && stepBox.getChildren().get(1) instanceof Label) {
                Label num = (Label) stepBox.getChildren().get(0);
                Label texto = (Label) stepBox.getChildren().get(1);
                num.getStyleClass().removeAll("active", "completed-number");
                texto.getStyleClass().remove("active");
                if (i == 0) {
                    stepBox.getStyleClass().add("active");
                    num.getStyleClass().add("active");
                    texto.getStyleClass().add("active");
                }
            }
        }
    }

    private void mostrarInfoEstado(String mensaje) {
        estadoProcesoLabel.setText(mensaje);
        estadoProcesoLabel.getStyleClass().remove("status-error");
        if (!estadoProcesoLabel.getStyleClass().contains("status-ok")) {
            estadoProcesoLabel.getStyleClass().add("status-ok");
        }
    }

    private String formatearSimboloVisual(String simbolo) {
        String normalizado = SimbolosAutomata.normalizarSimboloTransicion(simbolo);
        return SimbolosAutomata.esEpsilon(normalizado) ? SimbolosAutomata.EPSILON : normalizado;
    }

    private void mostrarError(String mensaje) {
        estadoProcesoLabel.setText(mensaje);
        estadoProcesoLabel.getStyleClass().remove("status-ok");
        if (!estadoProcesoLabel.getStyleClass().contains("status-error")) {
            estadoProcesoLabel.getStyleClass().add("status-error");
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validacion");
        alert.setHeaderText("Operacion no valida");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void marcarPasoCompleto(int paso) {
        if (stepBoxes != null && paso >= 1 && paso <= stepBoxes.length) {
            VBox stepBox = stepBoxes[paso - 1];
            
            // Marcar como completado
            if (!stepBox.getStyleClass().contains("completed")) {
                stepBox.getStyleClass().add("completed");
            }
            
            // Actualizar label del número con checkmark
            if (stepBox.getChildren().get(0) instanceof Label) {
                Label num = (Label) stepBox.getChildren().get(0);
                // Cambiar a checkmark visual usando CSS
                num.getStyleClass().removeAll("active");
                num.getStyleClass().add("completed-number");
            }
            
            // Activar el siguiente paso
            if (paso < stepBoxes.length) {
                VBox nextStep = stepBoxes[paso];
                if (!nextStep.getStyleClass().contains("active")) {
                    nextStep.getStyleClass().add("active");
                    if (nextStep.getChildren().get(0) instanceof Label) {
                        Label nextNum = (Label) nextStep.getChildren().get(0);
                        nextNum.getStyleClass().add("active");
                    }
                    if (nextStep.getChildren().get(1) instanceof Label) {
                        Label nextLabel = (Label) nextStep.getChildren().get(1);
                        nextLabel.getStyleClass().add("active");
                    }
                }
            }
        }
    }
}