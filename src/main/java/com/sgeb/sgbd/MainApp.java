package com.sgeb.sgbd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgeb/sgbd/view/main-view.fxml"));
        Parent root = loader.load(); // ⚠️ ne pas appeler FXMLLoader.load(url) directement
                                     // car ça peut provoquer l’erreur “Location is not set”
        primaryStage.setTitle("Test JavaFX - SGEB");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
