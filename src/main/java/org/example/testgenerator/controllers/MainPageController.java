package org.example.testgenerator.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Properties;

public class MainPageController {

    private Properties properties;
    private CreateTestController createTestController;
    private AnchorPane pane;

    @FXML
    private Button createButton;

    @FXML
    private Button verifyButton;

    @FXML
    private Button addButton;

    @FXML
    private Button instructionButton;

    @FXML
    private Button settingsButton;

    @FXML
    protected void createPressed() throws IOException {
        try {
            createTestController.setup(properties);
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            stage.setTitle("Creare test");
            stage.show();
        }
        catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("EROARE");
            alert.setContentText("A avut loc o eroare");
            alert.show();
        }
    }

    public void setup(CreateTestController createTestController, AnchorPane pane, Properties properties) {
        this.createTestController = createTestController;
        this.pane = pane;
        this.properties = properties;
    }
}