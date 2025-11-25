package com.sgeb.sgbd;

import com.sgeb.sgbd.controllers.PagePrincipaleController;
import com.sgeb.sgbd.dao.AdherentDAO;
import com.sgeb.sgbd.dao.DocumentDAO;
import com.sgeb.sgbd.dao.EmpruntDAO;
import com.sgeb.sgbd.model.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // --- Création des DAO ---
        DocumentDAO documentDAO = new DocumentDAO();
        AdherentDAO adherentDAO = new AdherentDAO(new EmpruntDAO(), documentDAO);
        EmpruntDAO empruntDAO = new EmpruntDAO();

        // --- Création des managers ---
        DocumentManager documentManager = new DocumentManager(documentDAO);
        AdherentManager adherentManager = new AdherentManager(empruntDAO, documentDAO);
        EmpruntManager empruntManager = new EmpruntManager(empruntDAO, documentDAO, adherentDAO);

        // --- Charger la page principale ---
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgeb/sgbd/view/PagePrincipaleAdmin.fxml"));
        Parent root = loader.load();

        PagePrincipaleController controller = loader.getController();
        controller.setManagers(documentManager, adherentManager, empruntManager);

        Scene scene = new Scene(root, 1175, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Gestion bibliothèque - SGEB");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
