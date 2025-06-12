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
            // Cargar la vista del formulario de tareas (temporalmente)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VistaPrincipal.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 720);


            // Configurar el ícono de la aplicación
            var iconURL = getClass().getResource("/images/logo.png");
            if (iconURL != null) {
                primaryStage.getIcons().add(new Image(iconURL.toExternalForm()));
            } else {
                System.out.println("⚠️ Icon image not found, using default icon");
            }

            primaryStage.setTitle("Gestor de Tareas");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(520);
            primaryStage.setMinHeight(700);
            primaryStage.show();

            System.out.println("✅ Aplicación iniciada correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("🚀 Iniciando Gestor de Tareas...");
        launch(args);
    }
}