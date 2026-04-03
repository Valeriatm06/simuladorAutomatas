package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;

/**
 * Operaciones de archivo: guardar y cargar autómatas.
 */
public class AutomataViewFileOps {
    private final Stage stage;
    private final AutomataController controller;
    private Runnable onAutomataLoaded;
    private Runnable onAutomataReset;

    public AutomataViewFileOps(Stage stage, AutomataController controller) {
        this.stage = stage;
        this.controller = controller;
    }

    public void setOnAutomataLoaded(Runnable callback) {
        this.onAutomataLoaded = callback;
    }

    public void setOnAutomataReset(Runnable callback) {
        this.onAutomataReset = callback;
    }

    public void guardarAutomata() {
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
            mostrarInfo("Automata guardado en " + file.getName());
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    public void cargarAutomata() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Cargar automata");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        try {
            controller.cargar(file.toPath());
            mostrarInfo("Automata cargado: " + file.getName());
            if (onAutomataLoaded != null) {
                onAutomataLoaded.run();
            }
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    public void mostrarAyuda() {
        Dialog<Void> ayuda = new Dialog<>();
        ayuda.setTitle("Ayuda - Simulador de Automatas");
        ayuda.setHeaderText("Guia rapida de uso");
        ayuda.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox contenido = new VBox(12);
        contenido.setPadding(new Insets(8, 4, 8, 4));
        contenido.getChildren().addAll(
                crearSeccionAyuda("1) Crear automata",
                        "Seleccione el tipo (DFA o NFA)",
                        "Defina el alfabeto separado por comas",
                        "Pulse 'Crear Automata'"),
                new Separator(),
                crearSeccionAyuda("2) Agregar estados",
                        "Pulse el boton de estado en la barra izquierda",
                        "Ingrese nombre y marque inicial/final si aplica",
                        "Haga click en el canvas para ubicarlo"),
                new Separator(),
                crearSeccionAyuda("3) Agregar transiciones",
                        "Pulse el boton de transicion",
                        "Seleccione origen, simbolo y destino",
                        "En NFA puede usar epsilon con el boton e o dejando vacio el simbolo"),
                new Separator(),
                crearSeccionAyuda("4) Evaluar y simular",
                        "Ingrese una palabra por linea en el panel derecho",
                        "Pulse 'Evaluar Todas'",
                        "Seleccione un resultado y use 'Siguiente paso' o 'Reproducir'"),
                new Separator(),
                crearSeccionAyuda("5) Guardar y cargar",
                        "Use los botones de guardar/cargar en la barra izquierda",
                        "El formato de archivo es JSON")
        );

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(420);
        scroll.setPrefViewportWidth(520);

        ayuda.getDialogPane().setContent(scroll);
        ayuda.showAndWait();
    }

    private VBox crearSeccionAyuda(String titulo, String... puntos) {
        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        VBox seccion = new VBox(6);
        seccion.getChildren().add(tituloLabel);
        for (String punto : puntos) {
            Label linea = new Label("• " + punto);
            linea.setWrapText(true);
            linea.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155;");
            seccion.getChildren().add(linea);
        }
        return seccion;
    }

    public void confirmarReinicio() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reiniciar automata");
        confirm.setHeaderText("Se eliminarán estados, transiciones y entradas");
        confirm.setContentText("¿Desea continuar?");
        Optional<ButtonType> respuesta = confirm.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            reiniciarAutomata();
        }
    }

    public void reiniciarAutomata() {
        try {
            controller.reiniciarAutomata();
            if (onAutomataReset != null) {
                onAutomataReset.run();
            }
            mostrarInfo("Lienzo limpio. Defina un nuevo automata");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operación no válida");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText("Operación completada");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
