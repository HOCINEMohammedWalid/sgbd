package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.util.PasswordUtil;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AdminRegisterController {

    private static final String ADMIN_IDENTIFIER = "admin@sgeb.fr";
    private static final String ADMIN_CONFIG_FILE = "admin_config.properties";

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorMessageLabel;

    @FXML
    private void initialize() {
        // CORRECTION 1: On n'affiche plus l'identifiant prédéfini ici
        // emailField.setText(ADMIN_IDENTIFIER);
        // emailField.setEditable(false); // CORRECTION 2: On le laisse modifiable
    }

    @FXML
    private void handleRegisterSubmit(ActionEvent event) {
        String emailSaisi = emailField.getText(); // Récupère l'email saisi par l'utilisateur
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        errorMessageLabel.setText("");

        // CORRECTION 3A: Vérification de l'identifiant secret prédéfini
        if (!emailSaisi.equals(ADMIN_IDENTIFIER)) {
            errorMessageLabel.setText("Identifiant administrateur incorrect.");
            return;
        }

        // Vérification de la cohérence des mots de passe
        if (password.isEmpty() || !password.equals(confirmPassword)) {
            errorMessageLabel.setText("Les mots de passe ne correspondent pas ou sont vides.");
            return;
        }

        try {
            // Empêcher le réenregistrement
            if (isAdminConfigured()) {
                errorMessageLabel.setText("L'administrateur est déjà configuré. Veuillez vous connecter.");
                return;
            }

            // 1. Hacher le mot de passe
            String hashedPassword = PasswordUtil.hashPassword(password);

            // 2. Sauvegarder (on utilise toujours ADMIN_IDENTIFIER comme clé stockée)
            saveAdminCredentials(ADMIN_IDENTIFIER, hashedPassword);

            errorMessageLabel.setText("Compte Admin créé ! Redirection...");

            // 3. Redirection
            handleBackToLogin(event);

        } catch (IOException e) {
            errorMessageLabel.setText("Erreur lors de la sauvegarde du fichier de configuration : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        loadView("/com/sgeb/sgbd/view/admin-auth-view.fxml", event);
    }

    private void loadView(String fxmlPath, Event event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML: " + fxmlPath);
        }
    }

    // --- Méthodes Util. ---

    public static boolean isAdminConfigured() {
        return new File(ADMIN_CONFIG_FILE).exists();
    }

    private void saveAdminCredentials(String identifier, String hashedPassword) throws IOException {
        Properties props = new Properties();
        props.setProperty("admin.email", identifier);
        props.setProperty("admin.hash", hashedPassword);

        try (FileOutputStream fos = new FileOutputStream(ADMIN_CONFIG_FILE)) {
            props.store(fos, "Configuration unique de l'administrateur système.");
        }
    }
}