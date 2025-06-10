package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import com.calendarfx.view.CalendarView;

public class CalendarioController {

    @FXML
    private Pane calendarioPane;

    public void initialize() {
        CalendarView calendarView = new CalendarView();
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarioPane.getChildren().add(calendarView);
    }
}

