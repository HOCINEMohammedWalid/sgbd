package com.sgeb.sgbd;

import java.time.LocalDate;
import java.util.ArrayList;

import com.sgeb.sgbd.controllers.ManagerLoader;
import com.sgeb.sgbd.controllers.PagePrincipaleController;
import com.sgeb.sgbd.dao.AdherentDAO;
import com.sgeb.sgbd.dao.DocumentDAO;
import com.sgeb.sgbd.dao.EmpruntDAO;
import com.sgeb.sgbd.model.*;
import com.sgeb.sgbd.model.enums.StatutAdherent;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // --- 1. SIMULATION DU CHOIX DE L'UTILISATEUR ---
        // **C'EST ICI QUE VOUS METTREZ VOTRE VRAIE LOGIQUE DE CONNEXION**
        boolean isAdmin = false; // Mettez 'true' pour tester l'Admin, 'false' pour le Non-Admin.

        // --- Création des DAO et Managers (inchangé) ---
        DocumentDAO documentDAO = new DocumentDAO();
        AdherentDAO adherentDAO = new AdherentDAO(new EmpruntDAO(), documentDAO);
        EmpruntDAO empruntDAO = new EmpruntDAO();

        Adherent adh = new Adherent(1, "Martin", "Lucas", "lucas.martin@mail.com",
                "12 Rue des Lilas", "0612345678", LocalDate.of(2022, 01, 15),
                StatutAdherent.ACTIF, new ArrayList<>());

        // adherentDAO.save(adh);

        DocumentManager documentManager = new DocumentManager(documentDAO);
        AdherentManager adherentManager = new AdherentManager(empruntDAO, documentDAO);
        EmpruntManager empruntManager = new EmpruntManager(empruntDAO, documentDAO, adherentDAO);

        // --- 2. Charger la page principale basée sur le rôle ---
        String fxmlPath;
        String title;
        FXMLLoader loader;
        Parent root;
        ManagerLoader controller;
        if (isAdmin) {
            fxmlPath = "/com/sgeb/sgbd/view/PagePrincipaleAdmin.fxml";
            title = "Gestion bibliothèque - ADMINISTRATEUR";
            loader = new FXMLLoader(getClass().getResource(fxmlPath));
            root = loader.load(); // **CORRECTION ICI :** Utiliser l'interface ManagerLoader
            controller = (ManagerLoader) loader.getController();
            controller.setManagers(documentManager, adherentManager, empruntManager);
        } else {
            // Assurez-vous que ce fichier FXML existe pour la vue Non-Admin
            fxmlPath = "/com/sgeb/sgbd/view/PagePrincipaleNonAdmin.fxml";
            title = "Gestion bibliothèque - ADHÉRENT";
            loader = new FXMLLoader(getClass().getResource(fxmlPath));
            root = loader.load(); // **CORRECTION ICI :** Utiliser l'interface ManagerLoader
            controller = (ManagerLoader) loader.getController();
            controller.setManagers(documentManager, adherentManager, empruntManager);
            controller.setAdherent(adh);
        }

        Scene scene = new Scene(root, 1175, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
