package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class WelcomeController implements ManagerLoader {

    private DocumentManager documentManager;
    private AdherentManager adherentManager;
    private EmpruntManager empruntManager;

    // Assurez-vous d'avoir des champs pour les inputs de login ici si nécessaire

    @Override
    public void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM) {
        if (this.documentManager == null && docM != null) {
            this.documentManager = docM;
            this.adherentManager = adhM;
            this.empruntManager = empM;
        }

    }

    // Méthode générique de redirection et d'injection
    private void loadMainView(String fxmlPath, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // --- INJECTION CRUCIALE ---
            ManagerLoader newController = loader.getController();
            newController.setManagers(documentManager, adherentManager, empruntManager);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Réinitialisation de la scène
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML: " + fxmlPath);
        }
    }

    @FXML
    private void handleAdminLogin(ActionEvent event) {
        // Dans une application réelle, vous valideriez l'utilisateur ici.
        String fxml = "/com/sgeb/sgbd/view/adminauthview.fxml";
        loadMainView(fxml, "Gestion bibliothèque - ADMINISTRATEUR", event);
    }

    // SIMULATION DU LOGIN ADHÉRENT
    @FXML
    private void handleAdherentLogin(ActionEvent event) {
        String fxml = "/com/sgeb/sgbd/view/adherentauthview.fxml";
        loadMainView(fxml, "Gestion bibliothèque - ADHÉRENT", event);
    }
}