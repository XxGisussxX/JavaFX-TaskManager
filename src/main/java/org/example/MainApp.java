package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.calendarfx.view.CalendarView;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        CalendarView calendarView = new CalendarView();
        Scene scene = new Scene(calendarView, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("CalendarFX Test");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
