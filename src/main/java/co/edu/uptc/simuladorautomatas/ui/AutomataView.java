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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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
    private TextField estadoNombreField;
    private ComboBox<String> estadoSeleccionadoCombo;
    private ComboBox<String> origenCombo;
    private ComboBox<String> destinoCombo;
    private TextField simboloField;
    private TextArea cadenasArea;
    private TextField trazaCadenaField;
    private ListView<String> resultadosList;
    private TextArea trazaArea;

    private boolean modoCrearEstado;
    private boolean nuevoEstadoInicial;
    private boolean nuevoEstadoAceptacion;
    private String estadoSeleccionadoCanvas;

    public AutomataView(Stage stage) {
        this.stage = stage;
        this.controller = new AutomataController();
    }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(crearHeader());
        root.setCenter(crearZonaCentral());
        root.setBottom(crearPanelResultados());
        inicializarAutomata();
        return root;
    }

    private Parent crearHeader() {
        Label titulo = new Label("Simulador y Analizador de Automatas (DFA / NFA)");
        titulo.getStyleClass().add("app-title");

        Label pasos = new Label("Flujo: 1) Crear automata  2) Definir estados  3) Definir transiciones  4) Evaluar cadenas");
        pasos.getStyleClass().add("app-subtitle");

        Button reiniciarBtn = new Button("Reiniciar");
        reiniciarBtn.getStyleClass().add("btn-danger");
        reiniciarBtn.setOnAction(e -> confirmarReinicio());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox filaSuperior = new HBox(10, titulo, spacer, reiniciarBtn);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(6, filaSuperior, pasos);
        header.getStyleClass().add("top-header");
        return header;
    }

    private Parent crearZonaCentral() {
        HBox contenedor = new HBox(14);
        contenedor.setPadding(new Insets(12, 12, 8, 12));

        VBox panelCanvasCard = new VBox(10);
        panelCanvasCard.getStyleClass().add("card");
        panelCanvasCard.setPadding(new Insets(12));

        HBox encabezadoCanvas = new HBox();
        Label canvasTitulo = new Label("Canvas del automata");
        canvasTitulo.getStyleClass().add("section-title");

        estadoProcesoLabel = new Label("Listo");
        estadoProcesoLabel.getStyleClass().add("status-chip");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        encabezadoCanvas.getChildren().addAll(canvasTitulo, spacer, estadoProcesoLabel);

        panelDibujo = crearPanelVisual();
        VBox.setVgrow(panelDibujo, Priority.ALWAYS);
        panelCanvasCard.getChildren().addAll(encabezadoCanvas, panelDibujo);
        HBox.setHgrow(panelCanvasCard, Priority.ALWAYS);

        VBox panelControlCard = new VBox(8);
        panelControlCard.getStyleClass().add("card");
        panelControlCard.setPadding(new Insets(10));
        panelControlCard.setPrefWidth(390);
        panelControlCard.setMinWidth(360);
        panelControlCard.setMaxWidth(430);

        Label controlesTitulo = new Label("Configuracion");
        controlesTitulo.getStyleClass().add("section-title");
        panelControlCard.getChildren().addAll(controlesTitulo, crearPanelControles());

        panelCanvasCard.prefWidthProperty().bind(contenedor.widthProperty().multiply(0.70));
        panelControlCard.prefWidthProperty().bind(contenedor.widthProperty().multiply(0.30));

        contenedor.getChildren().addAll(panelCanvasCard, panelControlCard);
        return contenedor;
    }

    private Pane crearPanelVisual() {
        Pane panel = new Pane();
        panel.setPrefSize(820, 560);
        panel.setMinSize(620, 460);
        panel.getStyleClass().add("canvas-pane");

        panel.setOnMouseClicked(event -> {
            if (modoCrearEstado) {
                crearEstadoDesdeCanvas(event.getX(), event.getY());
                return;
            }
            seleccionarEstadoEnCanvas(event.getX(), event.getY());
        });
        panel.widthProperty().addListener((obs, oldVal, newVal) -> redibujar());
        panel.heightProperty().addListener((obs, oldVal, newVal) -> redibujar());
        return panel;
    }

    private Parent crearPanelControles() {
        tipoCombo = new ComboBox<>();
        tipoCombo.getItems().addAll(TipoAutomata.DFA, TipoAutomata.NFA);
        tipoCombo.setValue(TipoAutomata.DFA);

        alfabetoField = new TextField();
        alfabetoField.setPromptText("Ej: a,b");

        estadoNombreField = new TextField();
        estadoNombreField.setPromptText("Nombre del estado");

        estadoSeleccionadoCombo = new ComboBox<>();
        origenCombo = new ComboBox<>();
        destinoCombo = new ComboBox<>();

        simboloField = new TextField();
        simboloField.setPromptText("Simbolo");

        cadenasArea = new TextArea();
        cadenasArea.setPrefRowCount(6);
        cadenasArea.setPromptText("Maximo 10 cadenas, una por linea");

        trazaCadenaField = new TextField();
        trazaCadenaField.setPromptText("Cadena para traza");

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("control-tabs");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab definicionTab = new Tab("1. Definicion", crearTabDefinicion());
        Tab estadosTab = new Tab("2. Estados", crearTabEstados());
        Tab transicionesTab = new Tab("3. Transiciones", crearTabTransiciones());
        Tab evaluacionTab = new Tab("4. Evaluacion", crearTabEvaluacion());
        Tab trazabilidadTab = new Tab("Trazabilidad", crearTabTrazabilidad());
        Tab persistenciaTab = new Tab("Persistencia", crearTabPersistencia());

        tabs.getTabs().addAll(definicionTab, estadosTab, transicionesTab, evaluacionTab, trazabilidadTab, persistenciaTab);
        return tabs;
    }

    private Parent crearTabDefinicion() {
        Button nuevoAutomataBtn = new Button("Crear automata");
        nuevoAutomataBtn.getStyleClass().add("btn-primary");
        nuevoAutomataBtn.setOnAction(e -> inicializarAutomata());

        VBox box = new VBox(8,
                fieldLabel("Tipo"), tipoCombo,
                fieldLabel("Alfabeto (separado por coma)"), alfabetoField,
                nuevoAutomataBtn
        );
        box.getStyleClass().add("section-box");
        return box;
    }

    private Parent crearTabEstados() {
        Button crearEstadoBtn = new Button("+ Estado normal");
        crearEstadoBtn.getStyleClass().add("btn-secondary");
        crearEstadoBtn.setOnAction(e -> activarModoCreacion(false, false, "Click en canvas para ubicar estado normal"));

        Button crearEstadoInicialBtn = new Button("+ Estado inicial");
        crearEstadoInicialBtn.getStyleClass().add("btn-secondary");
        crearEstadoInicialBtn.setOnAction(e -> activarModoCreacion(true, false, "Click en canvas para ubicar estado inicial"));

        Button crearEstadoAceptacionBtn = new Button("+ Estado aceptacion");
        crearEstadoAceptacionBtn.getStyleClass().add("btn-secondary");
        crearEstadoAceptacionBtn.setOnAction(e -> activarModoCreacion(false, true, "Click en canvas para ubicar estado de aceptacion"));

        Button marcarInicialBtn = new Button("Marcar inicial");
        marcarInicialBtn.getStyleClass().add("btn-ghost");
        marcarInicialBtn.setOnAction(e -> {
            String estado = estadoSeleccionadoCombo.getValue();
            if (estado == null) {
                mostrarError("Seleccione un estado");
                return;
            }
            try {
                controller.marcarInicial(estado);
                estadoSeleccionadoCanvas = estado;
                redibujar();
                mostrarInfoEstado("Estado inicial actualizado");
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        Button alternarAceptacionBtn = new Button("Alternar aceptacion");
        alternarAceptacionBtn.getStyleClass().add("btn-ghost");
        alternarAceptacionBtn.setOnAction(e -> {
            String estado = estadoSeleccionadoCombo.getValue();
            if (estado == null) {
                mostrarError("Seleccione un estado");
                return;
            }
            try {
                controller.alternarAceptacion(estado);
                estadoSeleccionadoCanvas = estado;
                redibujar();
                mostrarInfoEstado("Estado de aceptacion actualizado");
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        VBox box = new VBox(8,
                fieldLabel("Nombre del estado"), estadoNombreField,
                crearEstadoBtn,
                crearEstadoInicialBtn,
                crearEstadoAceptacionBtn,
                fieldLabel("Estado seleccionado"), estadoSeleccionadoCombo,
                new HBox(6, marcarInicialBtn, alternarAceptacionBtn)
        );
        box.getStyleClass().add("section-box");
        return box;
    }

    private Parent crearTabTransiciones() {
        Button transicionBtn = new Button("Agregar transicion");
        transicionBtn.getStyleClass().add("btn-secondary");
        transicionBtn.setOnAction(e -> {
            try {
                controller.agregarTransicion(origenCombo.getValue(), simboloField.getText(), destinoCombo.getValue());
                simboloField.clear();
                redibujar();
                mostrarInfoEstado("Transicion agregada");
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        VBox box = new VBox(8,
                fieldLabel("Origen"), origenCombo,
                fieldLabel("Simbolo"), simboloField,
                fieldLabel("Destino"), destinoCombo,
                transicionBtn
        );
        box.getStyleClass().add("section-box");
        return box;
    }

    private Parent crearTabEvaluacion() {
        Button evaluarBtn = new Button("Evaluar lote");
        evaluarBtn.getStyleClass().add("btn-primary");
        evaluarBtn.setOnAction(e -> evaluarCadenas());

        VBox box = new VBox(8,
                fieldLabel("Cadenas (una por linea)"), cadenasArea,
                evaluarBtn
        );
        box.getStyleClass().add("section-box");
        return box;
    }

    private Parent crearTabTrazabilidad() {
        Button trazaBtn = new Button("Generar traza");
        trazaBtn.getStyleClass().add("btn-secondary");
        trazaBtn.setOnAction(e -> generarTraza());

        VBox box = new VBox(8,
                fieldLabel("Cadena"), trazaCadenaField,
                trazaBtn
        );
        box.getStyleClass().add("section-box");
        return box;
    }

    private Parent crearTabPersistencia() {
        Button guardarBtn = new Button("Guardar JSON");
        guardarBtn.getStyleClass().add("btn-secondary");
        guardarBtn.setOnAction(e -> guardarAutomata());

        Button cargarBtn = new Button("Cargar JSON");
        cargarBtn.getStyleClass().add("btn-ghost");
        cargarBtn.setOnAction(e -> cargarAutomata());

        VBox box = new VBox(8, guardarBtn, cargarBtn);
        box.getStyleClass().add("section-box");
        return box;
    }

    private Parent crearPanelResultados() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(8, 12, 12, 12));

        Label titulo = new Label("Resultados y trazabilidad");
        titulo.getStyleClass().add("section-title");

        resultadosList = new ListView<>();
        resultadosList.setPrefHeight(110);
        resultadosList.getStyleClass().add("result-list");

        trazaArea = new TextArea();
        trazaArea.setPrefRowCount(4);
        trazaArea.setEditable(false);
        trazaArea.setPromptText("La traza detallada aparece aqui");

        VBox card = new VBox(8, titulo, resultadosList, fieldLabel("Traza paso a paso"), trazaArea);
        card.getStyleClass().add("card");
        panel.getChildren().add(card);
        return panel;
    }

    private void activarModoCreacion(boolean inicial, boolean aceptacion, String mensaje) {
        modoCrearEstado = true;
        nuevoEstadoInicial = inicial;
        nuevoEstadoAceptacion = aceptacion;
        mostrarInfoEstado(mensaje);
    }

    private void crearEstadoDesdeCanvas(double x, double y) {
        String nombre = estadoNombreField.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("Ingrese el nombre del estado antes de hacer click en el canvas");
            return;
        }
        try {
            controller.agregarEstado(
                    nombre,
                    nuevoEstadoInicial,
                    nuevoEstadoAceptacion,
                    ajustarXLogico(vistaALogicoX(x)),
                    ajustarYLogico(vistaALogicoY(y))
            );
            modoCrearEstado = false;
            estadoSeleccionadoCanvas = nombre;
            estadoNombreField.clear();
            actualizarCombos();
            redibujar();
            mostrarInfoEstado("Estado " + nombre + " creado");
            animarCanvasEntrada();
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
            estadoSeleccionadoCombo.setValue(encontrado);
            redibujar();
            mostrarInfoEstado("Estado seleccionado: " + encontrado);
        }
    }

    private Label fieldLabel(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("field-label");
        return label;
    }

    private void inicializarAutomata() {
        try {
            List<String> alfabeto = Arrays.stream(alfabetoField.getText().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            controller.nuevoAutomata(tipoCombo.getValue(), alfabeto);
            modoCrearEstado = false;
            estadoSeleccionadoCanvas = null;
            actualizarCombos();
            resultadosList.getItems().clear();
            trazaArea.clear();
            redibujar();
            mostrarInfoEstado("Automata creado. Continue con estados y transiciones");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void actualizarCombos() {
        List<String> nombres = controller.nombresEstados();
        origenCombo.getItems().setAll(nombres);
        destinoCombo.getItems().setAll(nombres);
        estadoSeleccionadoCombo.getItems().setAll(nombres);
        if (estadoSeleccionadoCanvas != null && nombres.contains(estadoSeleccionadoCanvas)) {
            estadoSeleccionadoCombo.setValue(estadoSeleccionadoCanvas);
        }
    }

    private void evaluarCadenas() {
        try {
            controller.validarAutomata();
            List<String> cadenas = Arrays.stream(cadenasArea.getText().split("\\R"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            List<EvaluacionCadenaResultado> resultados = controller.evaluarLote(cadenas);
            resultadosList.getItems().clear();
            for (EvaluacionCadenaResultado resultado : resultados) {
                resultadosList.getItems().add(resultado.getCadena() + " -> " + resultado.getEstadoTexto());
            }
            resaltarResultados();
            mostrarInfoEstado("Evaluacion completada: " + resultados.size() + " cadenas");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void generarTraza() {
        try {
            controller.validarAutomata();
            EvaluacionCadenaResultado resultado = controller.evaluarConTraza(trazaCadenaField.getText());
            trazaArea.setText(resultado.getCadena() + " -> " + resultado.getEstadoTexto() + "\n" + resultado.getTraza());
            mostrarInfoEstado("Traza generada");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
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
            actualizarCombos();
            redibujar();
            mostrarInfoEstado("Automata cargado: " + file.getName());
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
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
        FadeTransition ft = new FadeTransition(Duration.millis(240), resultadosList);
        ft.setFromValue(0.55);
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
        nuevoEstadoInicial = false;
        nuevoEstadoAceptacion = false;
        estadoSeleccionadoCanvas = null;
        alfabetoField.clear();
        estadoNombreField.clear();
        simboloField.clear();
        cadenasArea.clear();
        trazaCadenaField.clear();
        controller.nuevoAutomata(tipoCombo.getValue(), List.of());
        actualizarCombos();
        resultadosList.getItems().clear();
        trazaArea.clear();
        redibujar();
        mostrarInfoEstado("Lienzo limpio. Defina un nuevo automata");
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
}

