package co.edu.uptc.simuladorautomatas.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;

/**
 * Ventana modal para mostrar la función de transición extendida (δ*)
 */
public class VentanaFuncionTransicion {

    public static void mostrar(String funcionTransicionExtendida) {
        Stage ventana = new Stage();
        ventana.setTitle("Función de Transición Extendida (δ*)");
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setWidth(600);
        ventana.setHeight(500);

        // Crear área de texto con la función de transición
        TextArea textArea = new TextArea(funcionTransicionExtendida);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFont(Font.font("Courier New", 12));
        textArea.setStyle("-fx-control-inner-background: #F8FAFC; -fx-text-fill: #0F172A; -fx-padding: 12;");

        // Envolver en ScrollPane
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-padding: 0;");

        // Botones inferiores
        Button btnCopiar = new Button("Copiar al portapapeles");
        btnCopiar.getStyleClass().add("btn-action");
        btnCopiar.setOnAction(e -> copiarAlPortapapeles(funcionTransicionExtendida));
        btnCopiar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnCopiar, Priority.ALWAYS);

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.getStyleClass().add("btn-secondary");
        btnCerrar.setOnAction(e -> ventana.close());
        btnCerrar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnCerrar, Priority.ALWAYS);

        HBox botonesBox = new HBox(12, btnCopiar, btnCerrar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);
        botonesBox.setPadding(new Insets(12));
        botonesBox.setStyle("-fx-spacing: 12;");

        // Layout principal con VBox para mejor control vertical
        VBox mainLayout = new VBox(scrollPane, botonesBox);
        mainLayout.setPadding(new Insets(0));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainLayout.setStyle("-fx-spacing: 0;");

        Scene scene = new Scene(mainLayout);
        scene.getStylesheets().addAll(
                VentanaFuncionTransicion.class.getResource("/ui/automata-view.css").toExternalForm()
        );

        ventana.setScene(scene);
        ventana.showAndWait();
    }

    private static void copiarAlPortapapeles(String texto) {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(texto);
        clipboard.setContent(content);
    }
}
