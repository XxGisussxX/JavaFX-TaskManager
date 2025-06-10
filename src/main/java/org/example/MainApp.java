package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar la vista desde el archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VistaPrincipal.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            // Configurar el ícono de la aplicación
            var iconURL = getClass().getResource("/images/logo.png");
            if (iconURL != null) {
                primaryStage.getIcons().add(new Image(iconURL.toExternalForm()));
            } else {
                System.err.println("Icon image not found!");
            }

            primaryStage.setTitle("Gestor de Tareas");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


