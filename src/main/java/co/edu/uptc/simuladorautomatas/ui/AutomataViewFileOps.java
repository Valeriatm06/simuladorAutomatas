package co.edu.uptc.simuladorautomatas.ui;

import co.edu.uptc.simuladorautomatas.controller.AutomataController;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
            "   • Use los botones en la barra izquierda\n" +
            "   • Los archivos se guardan en formato JSON"
        );
        ayuda.showAndWait();
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
