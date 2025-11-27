package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.dao.AdherentDAO;
import com.sgeb.sgbd.dao.DocumentDAO;
import com.sgeb.sgbd.dao.EmpruntDAO;
import com.sgeb.sgbd.model.enums.StatutAdherent;
import com.sgeb.sgbd.util.PasswordUtil; // Pour le hachage
import com.sgeb.sgbd.model.*;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class AdherentRegisterController implements ManagerLoader {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField adresseField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorMessageLabel;

    // Ajoutez les champs des managers :
    private DocumentManager documentManager;
    private AdherentManager adherentManager;
    private EmpruntManager empruntManager;

    // ATTENTION: Initialisez correctement le DAO... (Ce champ est bien,
    // laissez-le.)
    private final AdherentDAO adherentDAO = new AdherentDAO(new EmpruntDAO(), new DocumentDAO());

    // Ajoutez la méthode d'injection
    @Override
    public void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM) {
        this.documentManager = docM;
        this.adherentManager = adhM;
        this.empruntManager = empM;

    }

    @FXML
    private void handleRegisterSubmit(ActionEvent event) {
        // 1. Récupération des données
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String telephone = telephoneField.getText();
        String adresse = adresseField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        errorMessageLabel.setText("");

        // 2. Validation des champs
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorMessageLabel.setText("Tous les champs obligatoires doivent être remplis.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessageLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            // 3. Hachage du mot de passe
            String hashedPassword = PasswordUtil.hashPassword(password);

            // 4. Création de l'objet Adherent (SANS le hash, en utilisant le constructeur
            // existant)
            // NOTE: Si le constructeur attend List<Emprunt> à la fin, fournissez-le.
            Adherent nouvelAdherent = new Adherent(
                    0, // ID
                    nom,
                    prenom,
                    email,
                    adresse,
                    telephone,
                    LocalDate.now(),
                    StatutAdherent.ACTIF,
                    new ArrayList<>() // Assurez-vous que cela correspond au dernier argument du constructeur Adherent
            );

            // 5. Enregistrement via la nouvelle méthode du DAO
            adherentDAO.saveWithPasswordHash(nouvelAdherent, hashedPassword);

            System.out.println("Inscription réussie pour: " + email);

            // 6. Redirection vers la page de connexion
            handleBackToLogin(event);

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'inscription : " + e.getMessage());
            errorMessageLabel.setText("Erreur d'enregistrement (Email peut-être déjà utilisé).");
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
            errorMessageLabel.setText("Erreur inattendue lors de l'inscription.");
        }
    }

    @FXML
    private void handleBackToLogin(Event event) {
        loadView("/com/sgeb/sgbd/view/adherentauthview.fxml", event);
    }

    // Méthode de navigation réutilisable (accepte Event pour les boutons et les
    // Labels)
    private void loadView(String fxmlPath, Event event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ManagerLoader) {
                ManagerLoader managerController = (ManagerLoader) controller;

                // Utilisez les managers stockés dans AdherentRegisterController
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