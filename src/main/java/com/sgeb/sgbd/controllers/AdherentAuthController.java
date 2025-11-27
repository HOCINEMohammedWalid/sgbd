package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.*;
import com.sgeb.sgbd.dao.AdherentDAO;
import com.sgeb.sgbd.dao.EmpruntDAO;
import com.sgeb.sgbd.dao.DocumentDAO;

import com.sgeb.sgbd.util.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import javafx.scene.control.Label;

public class AdherentAuthController implements ManagerLoader {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorMessageLabel;
    private DocumentManager documentManager;
    private AdherentManager adherentManager;
    private EmpruntManager empruntManager;

    private AdherentDAO adherentDAO;

    @Override
    public void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM) {
        this.documentManager = docM;
        this.adherentManager = adhM;
        this.empruntManager = empM;

        if (this.adherentDAO == null) { // N'initialise qu'une seule fois, au premier appel
            EmpruntDAO empruntDAO = new EmpruntDAO();
            DocumentDAO documentDAO = new DocumentDAO();
            this.adherentDAO = new AdherentDAO(empruntDAO, documentDAO);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String saisiEmail = usernameField.getText();
        String saisiMdp = passwordField.getText();

        try {
            // 1. Récupérer le hash stocké dans la BDD
            Optional<String[]> authData = adherentDAO.findByEmailWithHash(saisiEmail);

            if (authData.isPresent()) {
                String storedHash = authData.get()[1];

                // 2. Vérifier le mot de passe
                if (PasswordUtil.checkPassword(saisiMdp, storedHash)) {

                    Optional<Adherent> adherentOpt = adherentDAO.findByEmail(saisiEmail);

                    if (adherentOpt.isPresent()) {
                        Adherent adherentConnecte = adherentOpt.get();

                        System.out.println("Connexion Adhérent réussie : " + adherentConnecte.getNom());

                        // 4. Charger la vue et transmettre la session
                        loadMainView("/com/sgeb/sgbd/view/PagePrincipaleNonAdmin.fxml",
                                event,
                                adherentConnecte,
                                storedHash); // Passer le Hash pour le profil
                        return;
                    }
                }
            }

            // Échec
            System.err.println("Identifiants Adhérent incorrects.");

            errorMessageLabel.setText("Identifiant ou mot de passe incorrect.");
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'authentification : " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister(Event event) {
        loadView("/com/sgeb/sgbd/view/adherentregisterview.fxml", (Event) event);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        loadView("/com/sgeb/sgbd/view/Welcome.fxml", (Event) event);
    }

    /**
     * Charge la vue principale et injecte les managers et les données de session.
     */
    private void loadMainView(String fxmlPath, Event event, Adherent adherentConnecte, String motDePasseHache) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            // 1. Injection des Managers
            if (controller instanceof ManagerLoader) {
                ManagerLoader managerController = (ManagerLoader) controller;
                managerController.setManagers(this.documentManager, this.adherentManager, this.empruntManager);
            }

            // 2. 🔑 Injection des données de session (Adhérent et Hash)
            if (controller instanceof PagePrincipaleNonAdminController) {
                PagePrincipaleNonAdminController mainController = (PagePrincipaleNonAdminController) controller;

                mainController.setAdherentConnecte(adherentConnecte);

            }

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML: " + fxmlPath);
        }
    }

    // Méthode générique pour les autres vues (Register, Welcome)
    private void loadView(String fxmlPath, Event event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Tenter d'injecter les Managers si le contrôleur cible les accepte
            Object controller = loader.getController();
            if (controller instanceof ManagerLoader) {
                ManagerLoader managerController = (ManagerLoader) controller;
                managerController.setManagers(this.documentManager, this.adherentManager, this.empruntManager);
            }

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML: " + fxmlPath);
        }
    }
}