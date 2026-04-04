package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * Operaciones de archivo: guardar y cargar autómatas.
 * Refactorizado para maximizar la UI de JavaFX, evitar duplicidad y corregir bugs de diálogos.
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
        File file = configurarFileChooser("Guardar autómata").showSaveDialog(stage);
        if (file == null) return;

        try {
            controller.validarAutomata();
            controller.guardar(file.toPath());
            mostrarInfo("Autómata guardado exitosamente en:\n" + file.getName());
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    public void cargarAutomata() {
        File file = configurarFileChooser("Cargar autómata").showOpenDialog(stage);
        if (file == null) return;

        try {
            controller.cargar(file.toPath());
            if (onAutomataLoaded != null) {
                onAutomataLoaded.run();
            }
            mostrarInfo("Autómata cargado exitosamente:\n" + file.getName());
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    public void mostrarAyuda() {
        Dialog<Void> ayuda = new Dialog<>();
        ayuda.initOwner(stage); // Evita que el diálogo se pierda detrás de la ventana principal
        ayuda.setTitle("Ayuda - Simulador de Autómatas");
        ayuda.setHeaderText("Guía rápida de uso");
        ayuda.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox contenido = new VBox(16); // Mayor espaciado entre secciones para mejor lectura
        contenido.setPadding(new Insets(15, 20, 15, 10));
        
        contenido.getChildren().addAll(
                crearSeccionAyuda("1) Crear autómata",
                        "Seleccione el tipo (DFA o NFA).",
                        "Defina el alfabeto separado por comas.",
                        "Pulse 'Crear Automata'."),
                new Separator(),
                crearSeccionAyuda("2) Agregar estados",
                        "Pulse el botón de estado en la barra izquierda.",
                        "Ingrese nombre y marque inicial/final si aplica.",
                        "Haga click en el lienzo para ubicarlo."),
                new Separator(),
                crearSeccionAyuda("3) Agregar transiciones",
                        "Pulse el botón de transición.",
                        "Seleccione origen, símbolo y destino.",
                        "En NFA puede usar epsilon con el botón 'e' o dejando vacío el símbolo."),
                new Separator(),
                crearSeccionAyuda("4) Evaluar y simular",
                        "Ingrese una palabra por línea en el panel derecho.",
                        "Pulse 'Evaluar Todas'.",
                        "Seleccione un resultado y use 'Siguiente paso' o 'Reproducir'."),
                new Separator(),
                crearSeccionAyuda("5) Guardar y cargar",
                        "Use los botones de guardar/cargar en la barra izquierda.",
                        "El formato de archivo exportado es nativo (.json).")
        );

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        scroll.setPrefViewportHeight(450);
        scroll.setPrefViewportWidth(520);

        ayuda.getDialogPane().setContent(scroll);
        ayuda.showAndWait();
    }

    private VBox crearSeccionAyuda(String titulo, String... puntos) {
        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        tituloLabel.setPadding(new Insets(0, 0, 4, 0));

        VBox seccion = new VBox(6);
        seccion.getChildren().add(tituloLabel);
        
        for (String punto : puntos) {
            Label linea = new Label("• " + punto);
            linea.setWrapText(true);
            linea.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
            linea.setPadding(new Insets(0, 0, 0, 10)); // Sangría para los bullet points
            seccion.getChildren().add(linea);
        }
        return seccion;
    }

    public void confirmarReinicio() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        configurarAlerta(confirm);
        confirm.setTitle("Reiniciar autómata");
        confirm.setHeaderText("¿Desea limpiar el lienzo por completo?");
        confirm.setContentText("Se eliminarán todos los estados, transiciones y entradas actuales. Esta acción no se puede deshacer.");
        
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
            mostrarInfo("Lienzo limpio.\nPuede definir un nuevo autómata.");
        } catch (Exception ex) {
            mostrarError(ex.getMessage());
        }
    }

    // --- MÉTODOS PRIVADOS DE UTILIDAD ---

    private FileChooser configurarFileChooser(String titulo) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(titulo);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos JSON (*.json)", "*.json"));
        return chooser;
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        configurarAlerta(alert);
        alert.setTitle("Error");
        alert.setHeaderText("Operación no válida");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        configurarAlerta(alert);
        alert.setTitle("Información");
        alert.setHeaderText("Operación completada");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Aplica configuraciones estándar a las alertas para evitar bugs visuales de JavaFX.
     */
    private void configurarAlerta(Alert alert) {
        alert.initOwner(stage);
        // Fuerza al texto a usar el espacio que necesite (Evita que el texto largo termine en "...")
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    }
}