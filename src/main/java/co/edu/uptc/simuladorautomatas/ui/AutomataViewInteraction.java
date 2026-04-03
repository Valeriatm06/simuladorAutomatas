package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import co.edu.uptc.simuladorautomatas.model.Automata;
import co.edu.uptc.simuladorautomatas.model.Estado;
import co.edu.uptc.simuladorautomatas.model.SimbolosAutomata;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.Optional;

/**
 * Maneja la interacción del usuario: crear estados, crear transiciones, seleccionar y arrastrar.
 */
public class AutomataViewInteraction {
    private final AutomataController controller;
    private final AutomataViewDrawing drawingEngine;
    private Runnable onStatusChange;
    
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
        dialog.setHeaderText("¿Cuál es el nombre del estado?");

        VBox content = new VBox(12);
        content.setPadding(new Insets(12));

        HBox nombreBox = new HBox(8);
        Label labelNombre = new Label("Nombre:");
        labelNombre.setPrefWidth(80);
        TextField nombreField = new TextField();
        nombreField.setPromptText("Ej: q0");
        nombreField.setPrefWidth(250);
        nombreBox.getChildren().addAll(labelNombre, nombreField);

        CheckBox inicialCheckBox = new CheckBox("Estado Inicial");
        inicialCheckBox.setStyle("-fx-font-size: 12px;");
        
        CheckBox aceptacionCheckBox = new CheckBox("Estado de Aceptación (Final)");
        aceptacionCheckBox.setStyle("-fx-font-size: 12px;");

        content.getChildren().addAll(nombreBox, inicialCheckBox, aceptacionCheckBox);
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) {
                    mostrarError("El nombre del estado no puede estar vacío");
                    return null;
                }
                return new String[] {nombre, String.valueOf(inicialCheckBox.isSelected()), 
                                    String.valueOf(aceptacionCheckBox.isSelected())};
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(data -> {
            if (data != null && data[0] != null && !data[0].isEmpty()) {
                // Guardar datos para ser usados cuando el usuario haga click en el canvas
                estadoEnCreacionNombre = data[0];
                estadoEnCreacionInicial = Boolean.parseBoolean(data[1]);
                estadoEnCreacionAceptacion = Boolean.parseBoolean(data[2]);
                onEstadoCreado.run();
            }
        });
    }

    public void mostrarDialogoNuevaTransicion(Runnable onTransicionCreada) {
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
                mostrarInfo("Transición agregada");
                onTransicionCreada.run();
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    public void eliminarEstado(String estadoSeleccionado) {
        if (estadoSeleccionado == null) {
            mostrarError("Seleccione un estado para eliminar");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Estado");
        confirm.setHeaderText("¿Desea eliminar el estado '" + estadoSeleccionado + "'?");
        confirm.setContentText("Se eliminarán todas sus transiciones asociadas.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                controller.eliminarEstado(estadoSeleccionado);
                mostrarInfo("Estado eliminado");
                if (onStatusChange != null) {
                    onStatusChange.run();
                }
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        }
    }

    public String seleccionarEstadoEnCanvas(double x, double y) {
        Automata automata = controller.getAutomataActual();
        String encontrado = null;
        double mejor = Double.MAX_VALUE;
        double radio = drawingEngine.getRadioEscalado();
        double escala = drawingEngine.getEscala();
        
        for (Estado estado : automata.getEstados()) {
            double ex = estado.getX() * escala;
            double ey = estado.getY() * escala;
            double d = Math.hypot(ex - x, ey - y);
            if (d <= radio + 8 && d < mejor) {
                encontrado = estado.getNombre();
                mejor = d;
            }
        }
        return encontrado;
    }

    public Estado encontrarEstadoEnPosicion(double x, double y) {
        Automata automata = controller.getAutomataActual();
        double radio = drawingEngine.getRadioEscalado();
        double escala = drawingEngine.getEscala();
        
        for (Estado estado : automata.getEstados()) {
            double ex = estado.getX() * escala;
            double ey = estado.getY() * escala;
            double d = Math.hypot(ex - x, ey - y);
            if (d <= radio + 8) {
                return estado;
            }
        }
        return null;
    }

    public void crearEstadoEnPosicion(String nombre, boolean esInicial, boolean esAceptacion, 
                                     double x, double y) {
        try {
            double posX = Math.max(46, Math.min(900 - 46, drawingEngine.vistaALogicoX(x)));
            double posY = Math.max(46, Math.min(620 - 46, drawingEngine.vistaALogicoY(y)));
            
            controller.agregarEstado(nombre, esInicial, esAceptacion, posX, posY);
            mostrarInfo("Estado creado");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    public void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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
}
