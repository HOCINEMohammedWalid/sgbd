package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.*;
import com.sgeb.sgbd.util.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.io.File;

public class AdminAuthController implements ManagerLoader {

    // --- Constantes partagées ---
    private static final String ADMIN_IDENTIFIER = "admin@sgeb.fr";
    private static final String ADMIN_CONFIG_FILE = "admin_config.properties";
    private DocumentManager documentManager;
    private AdherentManager adherentManager;
    private EmpruntManager empruntManager;

    @Override
    public void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM) {
        this.documentManager = docM;
        this.adherentManager = adhM;
        this.empruntManager = empM;
    }

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        messageLabel.setText("");

        emailField.setText("");
        emailField.setEditable(true);

        // Vérification si l'admin est configuré
        if (!isAdminConfigured()) {
            messageLabel.setText("Administrateur non configuré. Veuillez vous enregistrer.");
        }
    }

    @FXML
    private void handleLoginSubmit(ActionEvent event) {
        String saisiEmail = emailField.getText();
        String saisiMdp = passwordField.getText();

        messageLabel.setText("");

        // 1. Vérification si le compte est créé
        if (!isAdminConfigured()) {
            messageLabel.setText("Erreur: Le compte administrateur n'a pas été configuré.");
            return;
        }

        // 2. Validation de l'identifiant saisi
        if (!saisiEmail.equals(ADMIN_IDENTIFIER)) {
            messageLabel.setText("Identifiant administrateur incorrect.");
            return;
        }

        try {
            // 3. Charger le hash
            Optional<String> storedHash = loadAdminHash();

            if (storedHash.isPresent()) {
                // 4. Vérifier le mot de passe
                if (PasswordUtil.checkPassword(saisiMdp, storedHash.get())) {

                    messageLabel.setText("Connexion réussie ! Redirection...");
                    loadView("/com/sgeb/sgbd/view/PagePrincipaleAdmin.fxml", event);

                } else {
                    messageLabel.setText("Mot de passe incorrect.");
                }
            } else {
                messageLabel.setText("Erreur: Hash non trouvé dans le fichier de configuration.");
            }

        }

        catch (IOException e) {
            messageLabel.setText("Erreur de lecture du fichier de configuration.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(javafx.scene.input.MouseEvent event) {
        if (!isAdminConfigured()) {
            loadView("/com/sgeb/sgbd/view/adminregisterview.fxml", event);
        } else {
            messageLabel.setText("Administrateur déjà configuré. Veuillez vous connecter.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        loadView("/com/sgeb/sgbd/view/Welcome.fxml", event);
    }

    // Dans AdminAuthController.java

    private void loadView(String fxmlPath, Event event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 🔑 1. Tenter d'injecter les Managers si le contrôleur cible les accepte
            Object controller = loader.getController();
            if (controller instanceof ManagerLoader) {
                ManagerLoader managerController = (ManagerLoader) controller;

                // ⚠️ AJOUT DE VÉRIFICATION DÉFENSIVE ET DE DÉBOGAGE
                if (this.documentManager == null || this.adherentManager == null || this.empruntManager == null) {
                    System.err.println(
                            "ERREUR GRAVE D'INJECTION: Un Manager est NULL dans AdminAuthController. Impossible de charger la vue correctement.");
                    // Vous pouvez ajouter une alerte à l'utilisateur ici.
                }

                // 🔑 2. Injection des Managers stockés
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

    // --- Méthodes Util. ---

    public static boolean isAdminConfigured() {
        return new File(ADMIN_CONFIG_FILE).exists();
    }

    private Optional<String> loadAdminHash() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ADMIN_CONFIG_FILE)) {
            props.load(fis);
            String hash = props.getProperty("admin.hash");
            return Optional.ofNullable(hash);
        }
    }
}