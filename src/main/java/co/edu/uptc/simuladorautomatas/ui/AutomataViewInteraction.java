package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.SimbolosAutomata;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Gestor de interacción del usuario con el canvas del autómata.
 * 
 * Responsable de:
 * - Mostrar diálogos para crear estados y transiciones
 * - Detectar y seleccionar estados en el canvas
 * - Manejar eliminación de estados
 * - Gestionar la creación de estados en posiciones del lienzo
 * 
 */
public class AutomataViewInteraction {
    private final AutomataController controller;
    private final AutomataViewDrawing drawingEngine;
    private Runnable onStatusChange;
    
    // Límites del canvas para posicionamiento de estados
    private static final double MARGEN_CANVAS = 46;
    private static final double ANCHO_CANVAS = 900;
    private static final double ALTO_CANVAS = 620;
    
    // Tolerancia para detectar clicks en estados
    private static final double TOLERANCIA_CLICK = 8;
    
    // Datos del estado siendo creado
    private String estadoEnCreacionNombre;
    private boolean estadoEnCreacionInicial;
    private boolean estadoEnCreacionAceptacion;

    public AutomataViewInteraction(AutomataController controller, AutomataViewDrawing drawingEngine) {
        this.controller = controller;
        this.drawingEngine = drawingEngine;
    }

    public void setOnStatusChange(Runnable callback) {
        this.onStatusChange = callback;
    }

    public void mostrarDialogoNuevoEstado(Runnable onEstadoCreado) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Estado");
        dialog.setHeaderText("Defina las propiedades del nuevo estado");
        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15, 10, 10, 10));

        // Campo para nombre del estado
        HBox nombreBox = new HBox(10);
        nombreBox.setAlignment(Pos.CENTER_LEFT);
        Label labelNombre = new Label("Nombre:");
        labelNombre.setPrefWidth(70);
        TextField nombreField = new TextField();
        nombreField.setPromptText("Ej: q0");
        nombreField.setPrefWidth(200);
        nombreBox.getChildren().addAll(labelNombre, nombreField);

        // Checkboxes para propiedades
        CheckBox inicialCheckBox = new CheckBox("Es Estado Inicial");
        inicialCheckBox.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
        
        CheckBox aceptacionCheckBox = new CheckBox("Es Estado de Aceptación (Final)");
        aceptacionCheckBox.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");

        content.getChildren().addAll(nombreBox, inicialCheckBox, aceptacionCheckBox);
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) {
                    mostrarError("El nombre del estado no puede estar vacío.");
                    return null;
                }
                return new String[] {
                    nombre, 
                    String.valueOf(inicialCheckBox.isSelected()), 
                    String.valueOf(aceptacionCheckBox.isSelected())
                };
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(data -> {
            if (data != null && data[0] != null && !data[0].isEmpty()) {
                estadoEnCreacionNombre = data[0];
                estadoEnCreacionInicial = Boolean.parseBoolean(data[1]);
                estadoEnCreacionAceptacion = Boolean.parseBoolean(data[2]);
                onEstadoCreado.run();
            }
        });
    }

    public void mostrarDialogoNuevaTransicion(Runnable onTransicionCreada) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        configurarAlerta(dialog);
        dialog.setTitle("Nueva Transición");
        dialog.setHeaderText("Configure los parámetros de la transición");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Selección de estado origen
        Label labelOrigen = new Label("Estado de origen:");
        labelOrigen.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        ComboBox<String> comboOrigen = new ComboBox<>();
        comboOrigen.getItems().addAll(controller.nombresEstados());
        comboOrigen.setMaxWidth(Double.MAX_VALUE);

        // Símbolo de transición
        Label labelSimbolo = new Label("Símbolo de transición:");
        labelSimbolo.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        TextField campoSimbolo = new TextField();
        campoSimbolo.setPromptText("Ej: a (Dejar vacío para ε en NFA)");
        
        Button btnEpsilon = new Button("ε");
        btnEpsilon.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");
        btnEpsilon.setOnAction(e -> campoSimbolo.setText(SimbolosAutomata.EPSILON));
        btnEpsilon.setTooltip(new Tooltip("Insertar transición Epsilon/Lambda"));
        
        HBox simboloBox = new HBox(8, campoSimbolo, btnEpsilon);
        HBox.setHgrow(campoSimbolo, Priority.ALWAYS);

        // Selección de estado destino
        Label labelDestino = new Label("Estado destino:");
        labelDestino.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        ComboBox<String> comboDestino = new ComboBox<>();
        comboDestino.getItems().addAll(controller.nombresEstados());
        comboDestino.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().addAll(
            labelOrigen, comboOrigen, 
            labelSimbolo, simboloBox,
            labelDestino, comboDestino
        );

        dialog.getDialogPane().setContent(content);
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String simboloIngresado = campoSimbolo.getText() == null ? "" : campoSimbolo.getText().trim();
                if (simboloIngresado.isEmpty()) {
                    simboloIngresado = SimbolosAutomata.EPSILON;
                }

                controller.agregarTransicion(comboOrigen.getValue(), simboloIngresado, comboDestino.getValue());
                mostrarInfo("Transición agregada exitosamente.");
                onTransicionCreada.run();
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    public void eliminarEstado(String estadoSeleccionado) {
        if (estadoSeleccionado == null) {
            mostrarError("Seleccione un estado en el lienzo para eliminarlo.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        configurarAlerta(confirm);
        confirm.setTitle("Eliminar Estado");
        confirm.setHeaderText("¿Desea eliminar el estado '" + estadoSeleccionado + "'?");
        confirm.setContentText("Se eliminarán todas las transiciones que entran y salen de este estado. Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                controller.eliminarEstado(estadoSeleccionado);
                mostrarInfo("Estado eliminado.");
                if (onStatusChange != null) {
                    onStatusChange.run();
                }
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    public String seleccionarEstadoEnCanvas(double x, double y) {
        Estado estadoEncontrado = buscarEstadoCercano(x, y);
        return (estadoEncontrado != null) ? estadoEncontrado.getNombre() : null;
    }

    public Estado encontrarEstadoEnPosicion(double x, double y) {
        return buscarEstadoCercano(x, y);
    }

    public void crearEstadoEnPosicion(String nombre, boolean esInicial, boolean esAceptacion, double x, double y) {
        try {
            double posX = Math.max(MARGEN_CANVAS, Math.min(ANCHO_CANVAS - MARGEN_CANVAS, drawingEngine.vistaALogicoX(x)));
            double posY = Math.max(MARGEN_CANVAS, Math.min(ALTO_CANVAS - MARGEN_CANVAS, drawingEngine.vistaALogicoY(y)));
            
            controller.agregarEstado(nombre, esInicial, esAceptacion, posX, posY);
            mostrarInfo("Estado creado exitosamente.");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }


    public void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        configurarAlerta(alert);
        alert.setTitle("Validación");
        alert.setHeaderText("Operación no válida");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void mostrarInfo(String mensaje) {
        if (onStatusChange != null) {
            onStatusChange.run();
        }
    }

    public String getEstadoEnCreacionNombre() {
        return estadoEnCreacionNombre;
    }

    public boolean isEstadoEnCreacionInicial() {
        return estadoEnCreacionInicial;
    }

    public boolean isEstadoEnCreacionAceptacion() {
        return estadoEnCreacionAceptacion;
    }

    public void limpiarEstadoEnCreacion() {
        estadoEnCreacionNombre = null;
        estadoEnCreacionInicial = false;
        estadoEnCreacionAceptacion = false;
    }

    private Estado buscarEstadoCercano(double x, double y) {
        Automata automata = controller.getAutomataActual();
        Estado encontrado = null;
        double mejor = Double.MAX_VALUE;
        double radio = drawingEngine.getRadioEscalado();
        double escala = drawingEngine.getEscala();
        
        for (Estado estado : automata.getEstados()) {
            double ex = estado.getX() * escala;
            double ey = estado.getY() * escala;
            double distancia = Math.hypot(ex - x, ey - y);
            
            if (distancia <= radio + TOLERANCIA_CLICK && distancia < mejor) {
                encontrado = estado;
                mejor = distancia;
            }
        }
        return encontrado;
    }

    private void configurarAlerta(Alert alert) {
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    }
}