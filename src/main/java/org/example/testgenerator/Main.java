package org.example.testgenerator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.testgenerator.controllers.CreateTestController;
import org.example.testgenerator.controllers.MainPageController;

import java.io.IOException;
import java.util.Properties;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("main-page.fxml"));
        AnchorPane root=loader.load();
        MainPageController mainPageController = loader.getController();

        FXMLLoader createLoader = new FXMLLoader();
        createLoader.setLocation(getClass().getResource("create-test-page.fxml"));
        AnchorPane createTest=createLoader.load();
        CreateTestController createTestController=createLoader.getController();

        Properties properties=new Properties();
        try{
            properties.load(Main.class.getResourceAsStream("/application.properties"));
        }
        catch (IOException e)
        {
            System.out.println("Cannot find properties "+e);
            return;
        }

        mainPageController.setup(createTestController, createTest, properties);
        stage.setScene(new Scene(root));
        stage.setTitle("Gestionare teste");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}