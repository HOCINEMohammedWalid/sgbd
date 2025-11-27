package com.sgeb.sgbd;

import com.sgeb.sgbd.controllers.ManagerLoader;
import com.sgeb.sgbd.dao.AdherentDAO;
import com.sgeb.sgbd.dao.DocumentDAO;
import com.sgeb.sgbd.dao.EmpruntDAO;
import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // 🔑 Bloc try-catch pour détecter l'échec d'instanciation (cause probable des
        // erreurs null)
        try {
            // --- 1. Création des DAO et Managers (Ordre corrigé pour les dépendances) ---

            // Instanciation des DAOs fondamentaux
            DocumentDAO documentDAO = new DocumentDAO();
            EmpruntDAO empruntDAO = new EmpruntDAO();

            // Instanciation du DAO qui dépend des autres
            // (Vérifiez l'ordre des arguments dans le constructeur de AdherentDAO)
            AdherentDAO adherentDAO = new AdherentDAO(empruntDAO, documentDAO);

            // Instanciation des Managers
            DocumentManager documentManager = new DocumentManager(documentDAO);
            AdherentManager adherentManager = new AdherentManager(empruntDAO, documentDAO);
            EmpruntManager empruntManager = new EmpruntManager(empruntDAO, documentDAO, adherentDAO);

            // --- 2. Vérification de l'état (Optionnel mais recommandé) ---
            if (documentManager == null || adherentManager == null || empruntManager == null) {
                // Cette exception sera capturée par le catch final si les objets sont null
                throw new IllegalStateException("L'un des Managers n'a pas pu être instancié correctement.");
            }

            // --- 3. Charger la PAGE DE CONNEXION (Welcome.fxml) ---
            String fxmlPath = "/com/sgeb/sgbd/view/Welcome.fxml";
            String title = "Gestion bibliothèque - Connexion";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 4. Injecter les Managers dans le WelcomeController
            Object controller = loader.getController();

            if (controller instanceof ManagerLoader) {
                ManagerLoader managerController = (ManagerLoader) controller;
                managerController.setManagers(documentManager, adherentManager, empruntManager);
            } else {
                System.err.println("ERREUR: Le WelcomeController n'implémente pas ManagerLoader.");
            }

            // --- 5. Afficher la scène ---
            Scene scene = new Scene(root, 1175, 600);

            // Assurez-vous que le chemin /styles/styles.css est correct
            String cssPath = getClass().getResource("/styles/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("--- ERREUR FATALE AU DÉMARRAGE DE L'APPLICATION ---");
            System.err.println("Cause possible: Problème de connexion BDD ou fichier manquant dans un DAO.");
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}