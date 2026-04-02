package co.edu.uptc.simuladorautomatas.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        AutomataView view = new AutomataView(stage);
        Scene scene = new Scene(view.build(), 1200, 760);
        scene.getStylesheets().add(getClass().getResource("/ui/automata-view.css").toExternalForm());
        stage.setTitle("Simulador y Analizador de Automatas Finitos");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

