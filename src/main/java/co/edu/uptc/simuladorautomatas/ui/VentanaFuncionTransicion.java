package co.edu.uptc.simuladorautomatas.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Ventana modal para mostrar la función de transición extendida (δ*).
 * 
 * Responsable de:
 * - Mostrar la representación textual de la función de transición extendida
 * - Permitir copiar el contenido al portapapeles
 * - Proporcionar una interfaz clara y ordenada
 * 
 */
public class VentanaFuncionTransicion {
    private static final double ANCHO_VENTANA = 600;
    private static final double ALTO_VENTANA = 500;
    private static final int FONT_SIZE = 12;
    private static final String FONT_FAMILY = "Courier New";
    private static final String COLOR_FONDO = "#F8FAFC";
    private static final String COLOR_TEXTO = "#0F172A";
    private static final int PADDING = 12;
    private static final String TITLE_WINDOW = "Función de Transición Extendida (δ*)";

    /**
     * Muestra una ventana modal con la función de transición extendida.
     */
    public static void mostrar(String funcionTransicionExtendida) {
        Stage ventana = new Stage();
        ventana.setTitle(TITLE_WINDOW);
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setWidth(ANCHO_VENTANA);
        ventana.setHeight(ALTO_VENTANA);

        // Crear área de texto con la función de transición
        TextArea textArea = crearAreaTexto(funcionTransicionExtendida);
        ScrollPane scrollPane = crearScrollPane(textArea);
        HBox botonesBox = crearBotonesInferiores(ventana, funcionTransicionExtendida);
        VBox mainLayout = crearLayoutPrincipal(scrollPane, botonesBox);

        Scene scene = new Scene(mainLayout);
        scene.getStylesheets().addAll(
                VentanaFuncionTransicion.class.getResource("/ui/automata-view.css").toExternalForm()
        );

        ventana.setScene(scene);
        ventana.showAndWait();
    }

    /**
     * Crea el área de texto con la función de transición.
     */
    private static TextArea crearAreaTexto(String contenido) {
        TextArea textArea = new TextArea(contenido);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFont(Font.font(FONT_FAMILY, FONT_SIZE));
        textArea.setStyle("-fx-control-inner-background: " + COLOR_FONDO + 
                         "; -fx-text-fill: " + COLOR_TEXTO + 
                         "; -fx-padding: " + PADDING + ";");
        return textArea;
    }

    private static ScrollPane crearScrollPane(TextArea textArea) {
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-padding: 0;");
        return scrollPane;
    }

    private static HBox crearBotonesInferiores(Stage ventana, String contenido) {
        Button btnCopiar = new Button("Copiar al portapapeles");
        btnCopiar.getStyleClass().add("btn-action");
        btnCopiar.setOnAction(e -> copiarAlPortapapeles(contenido));
        btnCopiar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnCopiar, Priority.ALWAYS);

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.getStyleClass().add("btn-secondary");
        btnCerrar.setOnAction(e -> ventana.close());
        btnCerrar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnCerrar, Priority.ALWAYS);

        HBox botonesBox = new HBox(12, btnCopiar, btnCerrar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);
        botonesBox.setPadding(new Insets(PADDING));
        botonesBox.setStyle("-fx-spacing: 12;");
        return botonesBox;
    }

    private static VBox crearLayoutPrincipal(ScrollPane scrollPane, HBox botonesBox) {
        VBox mainLayout = new VBox(scrollPane, botonesBox);
        mainLayout.setPadding(new Insets(0));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainLayout.setStyle("-fx-spacing: 0;");
        return mainLayout;
    }

    private static void copiarAlPortapapeles(String texto) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(texto);
        clipboard.setContent(content);
    }
}