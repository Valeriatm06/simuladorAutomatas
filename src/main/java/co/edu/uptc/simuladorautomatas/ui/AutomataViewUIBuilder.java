package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.model.TipoAutomata;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

/**
 * Constructor de componentes e interfaces de usuario.
 * 
 * Responsabilidades:
 * - Construir el encabezado (header) con título y botón de reinicio
 * - Crear indicadores visuales de progreso (steppers) para guiar el flujo
 * - Ensamblar la barra de herramientas flotante con acciones principales
 * - Construir paneles de configuración (tipo de autómata, alfabeto)
 * - Construir paneles de pruebas (entrada de palabras, resultados, simulación)
 * - Proporcionar componentes UI base con estilos consistentes
 * 
 * Constantes de dimensiones para mantener coherencia visual en toda la aplicación.
 * 
 */
public class AutomataViewUIBuilder {
    // Espaciados del header y contenedor principal
    private static final int SEPARACION_LOGO = 10;
    private static final int SEPARACION_STEPPER = 20;
    private static final int PADDING_HEADER = 8;
    
    // Dimensiones de la barra de herramientas flotante
    private static final int ANCHO_TOOLBAR = 60;
    private static final int ANCHO_BOTON_TOOLBAR = 36;
    
    // Dimensiones del panel de detalles (lateral derecho)
    private static final int ANCHO_PANEL_DETALLES = 300;
    private static final int ANCHO_MIN_PANEL = 250;
    private static final int ANCHO_MAX_PANEL = 350;
    
    // Espaciados internos de paneles
    private static final int SPACING_DETALLES = 8;
    private static final int SPACING_SECCION = 6;
    
    // Alturas preestablecidas
    private static final int ALTURA_PALABRAS = 8;
    private static final int ALTURA_RESULTADOS = 230;
    
    // Etapas del flujo de configuración
    private static final String[] PASOS = {"CREAR AUTOMATA", "DEFINIR ESTADOS", "DEFINIR TRANSICIONES", "EVALUAR CADENAS"};
    private static final int NUM_PASOS = 4;

    /**
     * Construye el encabezado principal de la aplicación.
     */
    public Parent crearHeader(Runnable onReiniciar) {
        Label titulo = new Label("Simulador y Analizador de Automatas (DFA / NFA)");
        titulo.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button reiniciarBtn = new Button("Reiniciar");
        reiniciarBtn.getStyleClass().add("btn-danger");
        reiniciarBtn.setOnAction(e -> onReiniciar.run());

        HBox filaSuperior = new HBox(10, titulo, spacer, reiniciarBtn);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        Region spacer2 = new Region();
        VBox.setVgrow(spacer2, Priority.ALWAYS);

        VBox header = new VBox(8, spacer2, filaSuperior);
        header.getStyleClass().add("top-header");
        header.setPadding(new Insets(8, 0, 8, 0));
        return header;
    }

    public HBox crearStepperVisual(VBox[] stepBoxesOut) {
        HBox stepsContainer = new HBox(20);
        stepsContainer.getStyleClass().add("stepper-container");
        stepsContainer.setPadding(new Insets(0, 0, 8, 0));

        String[] steps = {"CREAR AUTOMATA", "DEFINIR ESTADOS", "DEFINIR TRANSICIONES", "EVALUAR CADENAS"};
        VBox[] stepBoxes = new VBox[steps.length];

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

        // Copy reference
        if (stepBoxesOut != null && stepBoxesOut.length > 0) {
            for (int i = 0; i < stepBoxes.length; i++) {
                stepBoxesOut[i] = stepBoxes[i];
            }
        }

        return stepsContainer;
    }

    //Crea la barra de herramientas flotante vertical con acciones principales.
    public VBox crearToolbarFlotante(
            Runnable onAgregarEstado,
            Runnable onAgregarTransicion,
            Runnable onEliminar,
            Runnable onGuardar,
            Runnable onCargar,
            Runnable onAyuda) {
        VBox toolbar = new VBox(4);
        toolbar.getStyleClass().add("floating-toolbar");
        toolbar.setAlignment(Pos.TOP_CENTER);
        toolbar.setPrefWidth(60);
        toolbar.setMaxWidth(60);
        toolbar.setMinWidth(60);

        Button btnEstado = crearBotonToolbar("◯", "Agregar Estado", e -> onAgregarEstado.run());
        Button btnTransicion = crearBotonToolbar("→", "Agregar Transción", e -> onAgregarTransicion.run());
        Button btnBorrar = crearBotonToolbar("✕", "Eliminar Seleccionado", e -> onEliminar.run());
        
        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setPadding(new Insets(4, 0, 4, 0));
        sep.setStyle("-fx-padding: 4 0 4 0;");
        
        Button btnGuardar = crearBotonToolbar("💾", "Guardar Autómata", e -> onGuardar.run());
        Button btnCargar = crearBotonToolbar("📂", "Cargar Autómata", e -> onCargar.run());
        Button btnAyuda = crearBotonToolbar("❓", "Ayuda", e -> onAyuda.run());

        toolbar.getChildren().addAll(btnEstado, btnTransicion, btnBorrar, sep, btnGuardar, btnCargar, btnAyuda);
        return toolbar;
    }

    private Button crearBotonToolbar(String texto, String tooltip, EventHandler<ActionEvent> action) {
        Button btn = new Button(texto);
        btn.getStyleClass().add("btn-action");
        btn.setPrefSize(36, 36);
        btn.setOnAction(action);
        btn.setTooltip(new Tooltip(tooltip));
        return btn;
    }

    /**
     * Crea el StackPane principal con paneles de configuración y pruebas.
     * 
     * Utiliza StackPane para cambiar entre dos vistas:
     * 1. Panel de Configuración: seleccionar tipo, definir alfabeto
     * 2. Panel de Pruebas: ingresar palabras, evaluar, ver resultados
     */
    public StackPane crearPanelDetalles(
            ComboBox<TipoAutomata> tipoCombo,
            TextField alfabetoField,
            Runnable onCrearAutomata,
            TextArea palabrasArea,
            Runnable onEvaluarTodas,
            Runnable onLimpiar,
            Runnable onEpsilon,
            ListView<?> resultadosLoteList,
            Button btnSiguientePaso,
            Button btnReproducir,
            Button btnVerFuncion,
            Runnable onSiguientePaso,
            Runnable onReproducir,
            Runnable onVerFuncion,
            Label estadoProcesoLabel,
            VBox[] panelConfiguracionOut,
            VBox[] panelPruebasOut) {
        StackPane stackPane = crearContenedorDetalles();
        VBox panelConfiguracion = construirPanelConfiguracion(tipoCombo, alfabetoField, onCrearAutomata);
        VBox panelPruebas = construirPanelPruebas(
                palabrasArea,
                onEvaluarTodas,
                onLimpiar,
                onEpsilon,
                resultadosLoteList,
                btnSiguientePaso,
                btnReproducir,
                btnVerFuncion,
                onSiguientePaso,
                onReproducir,
                onVerFuncion,
                estadoProcesoLabel
        );

        stackPane.getChildren().addAll(panelConfiguracion, panelPruebas);
        publicarPanelesSalida(panelConfiguracionOut, panelPruebasOut, panelConfiguracion, panelPruebas);
        return stackPane;
    }

    /**
     * Crea el contenedor StackPane base para alternancia de paneles.
     * 
     * Dimensiones: 300x620 (ancho x alto) con límites mín/máx para redimensionamiento.
     */
    private StackPane crearContenedorDetalles() {
        StackPane stackPane = new StackPane();
        stackPane.setPrefWidth(300);
        stackPane.setMinWidth(250);
        stackPane.setMaxWidth(350);
        return stackPane;
    }

    /**
     * Crea el VBox base para los paneles de detalles con estilos comunes.
     */
    private VBox crearPanelBaseDetalles(boolean visible) {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("details-panel");
        panel.setPrefWidth(300);
        panel.setMinWidth(250);
        panel.setMaxWidth(350);
        panel.setVisible(visible);
        panel.setManaged(visible);
        return panel;
    }

    /**
     * Construye el panel de configuración inicial del autómata.
     * 
     * Permite al usuario:
     * - Seleccionar tipo de autómata (DFA/NFA)
     * - Ingresar alfabeto de entrada
     * - Crear un nuevo autómata
     */
    private VBox construirPanelConfiguracion(ComboBox<TipoAutomata> tipoCombo, TextField alfabetoField, Runnable onCrearAutomata) {
        VBox panelConfiguracion = crearPanelBaseDetalles(true);
        Label titulo = new Label("Configuración");
        titulo.getStyleClass().add("section-title");

        ScrollPane scrollDetalles = new ScrollPane(crearSeccionDefinicion(tipoCombo, alfabetoField, onCrearAutomata));
        scrollDetalles.setFitToWidth(true);
        VBox.setVgrow(scrollDetalles, Priority.ALWAYS);

        panelConfiguracion.getChildren().addAll(titulo, scrollDetalles);
        return panelConfiguracion;
    }

    /**
     * Crea la sección visual de definición de tipo y alfabeto.
     * 
     * Agrupa los controles para tipo de autómata, entrada de alfabeto
     * y botón para crear el autómata.
     */
    private VBox crearSeccionDefinicion(ComboBox<TipoAutomata> tipoCombo, TextField alfabetoField, Runnable onCrearAutomata) {
        VBox seccionDefinicion = new VBox(6);

        Label labelTipo = new Label("Tipo de Autómata:");
        labelTipo.getStyleClass().add("field-label");
        tipoCombo.getItems().addAll(TipoAutomata.DFA, TipoAutomata.NFA);
        tipoCombo.setValue(TipoAutomata.DFA);
        tipoCombo.setPrefWidth(Double.MAX_VALUE);

        Label labelAlfabeto = new Label("Alfabeto (a,b,c...):");
        labelAlfabeto.getStyleClass().add("field-label");
        alfabetoField.setPromptText("Ej: a,b");
        alfabetoField.setPrefWidth(Double.MAX_VALUE);

        Button btnCrearAutomata = new Button("Crear Autómata");
        btnCrearAutomata.getStyleClass().add("btn-primary");
        btnCrearAutomata.setPrefWidth(Double.MAX_VALUE);
        btnCrearAutomata.setOnAction(e -> onCrearAutomata.run());

        seccionDefinicion.getChildren().addAll(
                labelTipo, tipoCombo,
                labelAlfabeto, alfabetoField,
                new Separator(Orientation.HORIZONTAL),
                btnCrearAutomata
        );
        return seccionDefinicion;
    }

    /**
     * Construye el panel de pruebas para evaluar palabras contra el autómata.
     * 
     * Proporciona:
     * - Área de entrada para palabras (multilinea)
     * - Botones: Evaluar Todas, Limpiar, Agregar ε
     * - ListView de resultados del lote
     * - Controles de simulación: Siguiente paso, Reproducir, Ver δ*
     * - Indicador de estado del proceso actual
     */
    private VBox construirPanelPruebas(
            TextArea palabrasArea,
            Runnable onEvaluarTodas,
            Runnable onLimpiar,
            Runnable onEpsilon,
            ListView<?> resultadosLoteList,
            Button btnSiguientePaso,
            Button btnReproducir,
            Button btnVerFuncion,
            Runnable onSiguientePaso,
            Runnable onReproducir,
            Runnable onVerFuncion,
            Label estadoProcesoLabel) {
        VBox panelPruebas = crearPanelBaseDetalles(false);
        Label tituloPruebas = new Label("Palabras de Prueba");
        tituloPruebas.getStyleClass().add("section-title");

        configurarEntradaPalabras(palabrasArea);
        HBox botonesPruebas = crearBotonesPruebas(onEvaluarTodas, onLimpiar, onEpsilon);
        Label resultadosLabel = new Label("Resultados del lote:");
        resultadosLabel.getStyleClass().add("field-label");

        configurarResultadosLote(resultadosLoteList);
        HBox botonesSimulacion = configurarBotonesSimulacion(btnSiguientePaso, btnReproducir, btnVerFuncion, onSiguientePaso, onReproducir, onVerFuncion);
        configurarEstadoProceso(estadoProcesoLabel);

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
        return panelPruebas;
    }

    /**
     * Configura el TextArea para entrada de palabras de prueba.
     */
    private void configurarEntradaPalabras(TextArea palabrasArea) {
        palabrasArea.setPromptText("Ingrese una palabra por línea" + System.lineSeparator() +
                "Use ε/lambda para palabra vacía" + System.lineSeparator());
        palabrasArea.setWrapText(true);
        palabrasArea.setPrefRowCount(8);
    }

    /**
     * Crea la fila de botones para controlar las pruebas de palabras.
     * 
     * Botones: Evaluar Todas (primario), Limpiar (secundario), Epsilon (agregar ε).
     */
    private HBox crearBotonesPruebas(Runnable onEvaluarTodas, Runnable onLimpiar, Runnable onEpsilon) {
        Button btnEvaluarTodas = new Button("Evaluar Todas");
        btnEvaluarTodas.getStyleClass().add("btn-primary");
        btnEvaluarTodas.setMaxWidth(Double.MAX_VALUE);
        btnEvaluarTodas.setOnAction(e -> onEvaluarTodas.run());

        Button btnLimpiarText = new Button("Limpiar");
        btnLimpiarText.getStyleClass().add("btn-action");
        btnLimpiarText.setMaxWidth(Double.MAX_VALUE);
        btnLimpiarText.setOnAction(e -> onLimpiar.run());

        Button btnEpsilonPalabra = new Button("ε");
        btnEpsilonPalabra.getStyleClass().add("btn-secondary");
        btnEpsilonPalabra.setTooltip(new Tooltip("Agregar palabra vacía (epsilon)"));
        btnEpsilonPalabra.setOnAction(e -> onEpsilon.run());

        HBox botonesPruebas = new HBox(8, btnEvaluarTodas, btnLimpiarText, btnEpsilonPalabra);
        botonesPruebas.setStyle("-fx-spacing: 8;");
        HBox.setHgrow(btnEvaluarTodas, Priority.ALWAYS);
        HBox.setHgrow(btnLimpiarText, Priority.ALWAYS);
        return botonesPruebas;
    }

    /**
     * Configura el ListView para mostrar los resultados de evaluación del lote.
     */
    private void configurarResultadosLote(ListView<?> resultadosLoteList) {
        resultadosLoteList.setPrefHeight(230);
        resultadosLoteList.setPlaceholder(new Label("Evalúe cadenas para ver resultados"));
        VBox.setVgrow(resultadosLoteList, Priority.ALWAYS);
    }

    /**
     * Configura los botones de simulación y los organiza en una fila vertical.
     * 
     * Botones: Siguiente Paso, Reproducir automático, Ver δ* (función de transición).
     */
    private HBox configurarBotonesSimulacion(
            Button btnSiguientePaso,
            Button btnReproducir,
            Button btnVerFuncion,
            Runnable onSiguientePaso,
            Runnable onReproducir,
            Runnable onVerFuncion) {
        btnSiguientePaso.getStyleClass().add("btn-secondary");
        btnSiguientePaso.setDisable(true);
        btnSiguientePaso.setMaxWidth(Double.MAX_VALUE);
        btnSiguientePaso.setOnAction(e -> onSiguientePaso.run());

        btnReproducir.getStyleClass().add("btn-secondary");
        btnReproducir.setDisable(true);
        btnReproducir.setMaxWidth(Double.MAX_VALUE);
        btnReproducir.setOnAction(e -> onReproducir.run());

        btnVerFuncion.getStyleClass().add("btn-action");
        btnVerFuncion.setDisable(true);
        btnVerFuncion.setMaxWidth(Double.MAX_VALUE);
        btnVerFuncion.setTooltip(new Tooltip("Mostrar δ* (función de transición extendida)"));
        btnVerFuncion.setOnAction(e -> onVerFuncion.run());

        VBox simulacionBox = new VBox(8);
        simulacionBox.getChildren().addAll(btnSiguientePaso, btnReproducir, btnVerFuncion);

        HBox.setHgrow(simulacionBox, Priority.ALWAYS);
        HBox botonesSimulacion = new HBox(simulacionBox);
        botonesSimulacion.setStyle("-fx-spacing: 0;");
        botonesSimulacion.setMaxWidth(Double.MAX_VALUE);
        return botonesSimulacion;
    }

    /**
     * Configura el Label que muestra el estado actual del proceso de evaluación.
     */
    private void configurarEstadoProceso(Label estadoProcesoLabel) {
        estadoProcesoLabel.setMaxWidth(Double.MAX_VALUE);
        estadoProcesoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B; -fx-wrap-text: true;");
        estadoProcesoLabel.getStyleClass().add("status-chip");
    }

    /**
     * Copia referencias de paneles en los arrays de salida para acceso desde el exterior.
     */
    private void publicarPanelesSalida(
            VBox[] panelConfiguracionOut,
            VBox[] panelPruebasOut,
            VBox panelConfiguracion,
            VBox panelPruebas) {
        if (panelConfiguracionOut != null && panelConfiguracionOut.length > 0) {
            panelConfiguracionOut[0] = panelConfiguracion;
        }
        if (panelPruebasOut != null && panelPruebasOut.length > 0) {
            panelPruebasOut[0] = panelPruebas;
        }
    }

    /**
     * Alterna a la vista del panel de configuración (oculta panel de pruebas).
     * 
     * Utilizado después de crear automata para mostrar la configuración nuevamente.
     */
    public void mostrarPanelConfiguracion(VBox panelConfiguracion, VBox panelPruebas) {
        panelConfiguracion.setVisible(true);
        panelConfiguracion.setManaged(true);
        panelPruebas.setVisible(false);
        panelPruebas.setManaged(false);
    }

    /**
     * Alterna a la vista del panel de pruebas (oculta panel de configuración).
     * 
     * Utilizado después de crear autómata para mostrar pruebas.
     */
    public void mostrarPanelPruebas(VBox panelConfiguracion, VBox panelPruebas) {
        panelConfiguracion.setVisible(false);
        panelConfiguracion.setManaged(false);
        panelPruebas.setVisible(true);
        panelPruebas.setManaged(true);
    }
}
