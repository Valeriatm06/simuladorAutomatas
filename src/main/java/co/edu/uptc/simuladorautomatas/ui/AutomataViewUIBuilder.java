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
 * Responsable de construir todos los paneles y componentes UI.
 * Encapsula: header, toolbar, steppers, paneles de configuración y pruebas.
 */
public class AutomataViewUIBuilder {

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
            Runnable onSiguientePaso,
            Runnable onReproducir,
            Label estadoProcesoLabel,
            VBox[] panelConfiguracionOut,
            VBox[] panelPruebasOut) {

        StackPane stackPane = new StackPane();
        stackPane.setPrefWidth(300);
        stackPane.setMinWidth(250);
        stackPane.setMaxWidth(350);

        // PANEL 1: Configuración
        VBox panelConfiguracion = new VBox(8);
        panelConfiguracion.getStyleClass().add("details-panel");
        panelConfiguracion.setPrefWidth(300);
        panelConfiguracion.setMinWidth(250);
        panelConfiguracion.setMaxWidth(350);

        Label titulo = new Label("Configuración");
        titulo.getStyleClass().add("section-title");

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

        ScrollPane scrollDetalles = new ScrollPane(seccionDefinicion);
        scrollDetalles.setFitToWidth(true);
        VBox.setVgrow(scrollDetalles, Priority.ALWAYS);

        panelConfiguracion.getChildren().addAll(titulo, scrollDetalles);

        // PANEL 2: Pruebas
        VBox panelPruebas = new VBox(8);
        panelPruebas.getStyleClass().add("details-panel");
        panelPruebas.setPrefWidth(300);
        panelPruebas.setMinWidth(250);
        panelPruebas.setMaxWidth(350);
        panelPruebas.setVisible(false);
        panelPruebas.setManaged(false);

        Label tituloPruebas = new Label("Palabras de Prueba");
        tituloPruebas.getStyleClass().add("section-title");

        palabrasArea.setPromptText("Ingrese una palabra por línea" + System.lineSeparator() +
                "Use ε/lambda para palabra vacía" + System.lineSeparator());
        palabrasArea.setWrapText(true);
        palabrasArea.setPrefRowCount(8);

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

        Label resultadosLabel = new Label("Resultados del lote:");
        resultadosLabel.getStyleClass().add("field-label");

        resultadosLoteList.setPrefHeight(230);
        resultadosLoteList.setPlaceholder(new Label("Evalúe cadenas para ver resultados"));

        btnSiguientePaso.getStyleClass().add("btn-secondary");
        btnSiguientePaso.setDisable(true);
        btnSiguientePaso.setMaxWidth(Double.MAX_VALUE);
        btnSiguientePaso.setOnAction(e -> onSiguientePaso.run());

        btnReproducir.getStyleClass().add("btn-secondary");
        btnReproducir.setDisable(true);
        btnReproducir.setMaxWidth(Double.MAX_VALUE);
        btnReproducir.setOnAction(e -> onReproducir.run());

        HBox botonesSimulacion = new HBox(8, btnSiguientePaso, btnReproducir);
        botonesSimulacion.setStyle("-fx-spacing: 8;");
        botonesSimulacion.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnSiguientePaso, Priority.ALWAYS);
        HBox.setHgrow(btnReproducir, Priority.ALWAYS);

        VBox.setVgrow(resultadosLoteList, Priority.ALWAYS);

        estadoProcesoLabel.setMaxWidth(Double.MAX_VALUE);
        estadoProcesoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B; -fx-wrap-text: true;");
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
        
        if (panelConfiguracionOut != null && panelConfiguracionOut.length > 0) {
            panelConfiguracionOut[0] = panelConfiguracion;
        }
        if (panelPruebasOut != null && panelPruebasOut.length > 0) {
            panelPruebasOut[0] = panelPruebas;
        }

        return stackPane;
    }

    public void mostrarPanelConfiguracion(VBox panelConfiguracion, VBox panelPruebas) {
        panelConfiguracion.setVisible(true);
        panelConfiguracion.setManaged(true);
        panelPruebas.setVisible(false);
        panelPruebas.setManaged(false);
    }

    public void mostrarPanelPruebas(VBox panelConfiguracion, VBox panelPruebas) {
        panelConfiguracion.setVisible(false);
        panelConfiguracion.setManaged(false);
        panelPruebas.setVisible(true);
        panelPruebas.setManaged(true);
    }
}
