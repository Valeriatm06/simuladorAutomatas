package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import co.edu.uptc.simuladorautomatas.logic.EvaluacionCadenaResultado;
import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import co.edu.uptc.simuladorautomatas.model.Transicion;
import javafx.animation.FadeTransition;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    private ComboBox<TipoAutomata> tipoCombo;
    private TextField alfabetoField;
    private TextArea palabrasArea;
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

    public AutomataView(Stage stage) {
        this.stage = stage;
        this.controller = new AutomataController();
    }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root-with-grid");
        root.setTop(crearHeader());
        root.setCenter(crearZonaPrincipal());
        root.setBottom(crearTimelineEvaluacion());
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

        // Fila de botones de operaciones
        HBox filaOperaciones = new HBox(12);
        filaOperaciones.setAlignment(Pos.CENTER_LEFT);
        filaOperaciones.setPadding(new Insets(8, 0, 0, 0));
        
        Button btnGuardar = new Button("💾 Guardar Autómata");
        btnGuardar.setPrefWidth(150);
        btnGuardar.setStyle("-fx-font-size: 12px; -fx-padding: 10 16;");
        btnGuardar.getStyleClass().add("btn-primary");
        btnGuardar.setOnAction(e -> guardarAutomata());

        Button btnCargar = new Button("📂 Cargar Autómata");
        btnCargar.setPrefWidth(150);
        btnCargar.setStyle("-fx-font-size: 12px; -fx-padding: 10 16;");
        btnCargar.getStyleClass().add("btn-primary");
        btnCargar.setOnAction(e -> cargarAutomata());

        Button btnAyuda = new Button("❓ Ayuda");
        btnAyuda.setPrefWidth(100);
        btnAyuda.setStyle("-fx-font-size: 12px; -fx-padding: 10 16;");
        btnAyuda.getStyleClass().add("btn-primary");
        btnAyuda.setOnAction(e -> mostrarAyuda());

        filaOperaciones.getChildren().addAll(btnGuardar, btnCargar, btnAyuda);

        VBox header = new VBox(8, filaSuperior, filaOperaciones);
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

        Button btnEstado = crearBotonToolbar("⊕", "Agregar Estado", e -> activarModoCreacionEstado());
        Button btnTransicion = crearBotonToolbar("→", "Agregar Transción", e -> activarModoCreacionTransicion());
        Button btnBorrar = crearBotonToolbar("✕", "Eliminar Seleccionado", e -> eliminarElementoSeleccionado());
        Button btnSeleccionar = crearBotonToolbar("◯", "Seleccionar", e -> activarModoSeleccion());

        toolbar.getChildren().addAll(btnEstado, btnTransicion, btnBorrar, btnSeleccionar);
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
        palabrasArea.setPromptText("Ingrese una palabra por línea\\nEj:\\naba\\nabb\\nbaab");
        palabrasArea.setWrapText(true);
        palabrasArea.setPrefRowCount(10);
        VBox.setVgrow(palabrasArea, Priority.ALWAYS);

        Button btnEvaluarTodas = new Button("Evaluar Todas");
        btnEvaluarTodas.getStyleClass().add("btn-primary");
        btnEvaluarTodas.setPrefWidth(Double.MAX_VALUE);
        btnEvaluarTodas.setOnAction(e -> evaluarPalabras());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.getStyleClass().add("btn-action");
        btnLimpiar.setPrefWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> palabrasArea.clear());

        HBox botonesPruebas = new HBox(8, btnEvaluarTodas, btnLimpiar);
        botonesPruebas.setStyle("-fx-spacing: 8;");
        HBox.setHgrow(btnEvaluarTodas, Priority.ALWAYS);
        HBox.setHgrow(btnLimpiar, Priority.ALWAYS);

        panelPruebas.getChildren().addAll(tituloPruebas, palabrasArea, botonesPruebas);

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
            redibujar();
            mostrarInfoEstado("Autómata " + tipoCombo.getValue() + " creado. Agregue estados con el toolbar.");
            
            // Cambiar al panel de pruebas
            mostrarPanelPruebas();
            palabrasArea.clear();
            
            // Marcar paso 1 como completado
            marcarPasoCompleto(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("Error al crear autómata: " + ex.getMessage());
        }
    }
    
    private void evaluarPalabras() {
        String contenido = palabrasArea.getText().trim();
        if (contenido.isEmpty()) {
            mostrarError("Ingrese palabras para evaluar");
            return;
        }
        
        String[] palabras = contenido.split("\n");
        StringBuilder resultados = new StringBuilder();
        
        for (String palabra : palabras) {
            palabra = palabra.trim();
            if (!palabra.isEmpty()) {
                try {
                    EvaluacionCadenaResultado resultado = controller.evaluarConTraza(palabra);
                    resultados.append(palabra).append(" -> ").append(resultado.getEstadoTexto()).append("\n");
                } catch (Exception ex) {
                    resultados.append(palabra).append(" -> ERROR: ").append(ex.getMessage()).append("\n");
                }
            }
        }
        
        // Mostrar resultados en un diálogo
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Resultados de Evaluación");
        alert.setHeaderText("Evaluación de Palabras");
        alert.setContentText(resultados.toString());
        alert.showAndWait();
        
        // Marcar paso 4 como completado cuando se evalúan palabras
        if (!paso4Completado) {
            marcarPasoCompleto(4);
            paso4Completado = true;
        }
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
            if (modoActual == 3) {
                seleccionarEstadoEnCanvas(event.getX(), event.getY());
                return;
            }
            seleccionarEstadoEnCanvas(event.getX(), event.getY());
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

    private Parent crearTimelineEvaluacion() {
        VBox contenedor = new VBox(8);
        contenedor.setPadding(new Insets(12));
        contenedor.getStyleClass().add("timeline-container");
        VBox.setVgrow(contenedor, Priority.NEVER);

        // Título
        Label titulo = new Label("Evaluación de Cadenas");
        titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        // Panel de entrada
        HBox panelEntrada = new HBox(8);
        panelEntrada.setAlignment(Pos.CENTER_LEFT);
        
        TextField cadenaInputField = new TextField();
        cadenaInputField.setPromptText("Ingrese una cadena para evaluar (ej: 10101010)");
        cadenaInputField.setStyle("-fx-font-size: 12px; -fx-padding: 8;");
        HBox.setHgrow(cadenaInputField, Priority.ALWAYS);

        Button evaluarBtn = new Button("Evaluar");
        evaluarBtn.getStyleClass().add("btn-primary");
        evaluarBtn.setPrefWidth(100);
        evaluarBtn.setStyle("-fx-font-size: 12px; -fx-padding: 8 16;");
        evaluarBtn.setOnAction(e -> evaluarCadenaUnica(cadenaInputField.getText(), contenedor));

        panelEntrada.getChildren().addAll(cadenaInputField, evaluarBtn);

        // Panel de resultados
        VBox panelResultados = new VBox(8);
        panelResultados.setStyle(
            "-fx-border-color: #E2E8F0; " +
            "-fx-border-width: 1; " +
            "-fx-background-color: #F8FAFC; " +
            "-fx-border-radius: 8; " +
            "-fx-padding: 12;"
        );
        panelResultados.setPrefHeight(120);
        panelResultados.setMinHeight(80);

        Label etiquetaResultados = new Label("Resultado:");
        etiquetaResultados.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #475569;");

        estadoProcesoLabel = new Label("Ingrese una cadena y presione 'Evaluar'");
        estadoProcesoLabel.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #64748B; " +
            "-fx-wrap-text: true;"
        );
        estadoProcesoLabel.getStyleClass().add("status-chip");

        panelResultados.getChildren().addAll(etiquetaResultados, estadoProcesoLabel);
        VBox.setVgrow(panelResultados, Priority.ALWAYS);

        contenedor.getChildren().addAll(titulo, panelEntrada, panelResultados);
        return contenedor;
    }

    private void evaluarCadenaUnica(String cadena, VBox contenedor) {
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
            
        } catch (Exception ex) {
            estadoProcesoLabel.setText("Error: " + ex.getMessage());
            estadoProcesoLabel.getStyleClass().removeAll("status-ok");
            estadoProcesoLabel.getStyleClass().add("status-error");
        }
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
        campoSimbolo.setPromptText("ej: a");

        Label labelDestino = new Label("Estado destino:");
        labelDestino.getStyleClass().add("field-label");
        ComboBox<String> comboDestino = new ComboBox<>();
        comboDestino.getItems().addAll(controller.nombresEstados());

        content.getChildren().addAll(labelOrigen, comboOrigen, labelSimbolo, campoSimbolo,
                                    labelDestino, comboDestino);

        dialog.getDialogPane().setContent(content);
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                controller.agregarTransicion(comboOrigen.getValue(), campoSimbolo.getText(),
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

    private void activarModoSeleccion() {
        modoActual = 3;
        mostrarInfoEstado("Modo selección activado - Click en un estado para seleccionar");
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
            "   • Ingrese el símbolo del alfabeto\n\n" +
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
        Automata automata = controller.getAutomataActual();

        for (Transicion transicion : automata.getTransiciones()) {
            dibujarTransicion(transicion);
        }
        for (Estado estado : automata.getEstados()) {
            dibujarEstado(estado);
        }
    }

    private void dibujarEstado(Estado estado) {
        boolean seleccionado = estado.getNombre().equals(estadoSeleccionadoCanvas);
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

        Circle principal = new Circle(x, y, radio, Color.web("#F8FAFC"));
        principal.setStroke(seleccionado ? Color.web("#2563EB") : Color.web("#1F2937"));
        principal.setStrokeWidth(seleccionado ? 3.0 : 1.7);

        if (estado.isEsInicial()) {
            principal.setFill(Color.web("#E0F2FE"));
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
            Text etiqueta = new Text(transicion.getSimbolo());
            etiqueta.setFill(Color.web("#1E40AF"));
            etiqueta.setX(ox - 2);
            etiqueta.setY(oy - radio - 30);
            panelDibujo.getChildren().addAll(loop, etiqueta);
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

        Text etiqueta = new Text(transicion.getSimbolo());
        etiqueta.setFill(Color.web("#1D4ED8"));
        etiqueta.setX(controlX - (etiqueta.getLayoutBounds().getWidth() / 2));
        etiqueta.setY(controlY - 10);

        panelDibujo.getChildren().addAll(curva, punta, etiqueta);
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
        alfabetoField.clear();
        palabrasArea.clear();
        tipoCombo.setValue(TipoAutomata.DFA);
        controller.reiniciarAutomata();
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

