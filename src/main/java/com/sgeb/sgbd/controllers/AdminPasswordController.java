package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.util.PasswordUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Pour charger le FXML
import javafx.scene.Parent; // Pour la racine de la nouvelle vue
import javafx.scene.Scene; // Pour définir la nouvelle scène (si vous changez le stage)
import javafx.scene.Node; // Pour accéder au Stage depuis l'ActionEvent
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import java.io.*;
import java.util.Properties;
import com.sgeb.sgbd.model.*;

public class AdminPasswordController implements ManagerLoader {

    private static final String ADMIN_CONFIG_FILE = "admin_config.properties";
    private static final String ADMIN_IDENTIFIER = "admin@sgeb.fr";

    @FXML
    private PasswordField ancienPasswordField;
    @FXML
    private PasswordField nouveauPasswordField;
    @FXML
    private PasswordField confirmerPasswordField;

    @FXML
    private Label messageLabel;

    private String hashMotDePasseActuel;

    private DocumentManager documentManager;
    private AdherentManager adherentManager;
    private EmpruntManager empruntManager;

    public void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM) {
        this.documentManager = docM;
        this.adherentManager = adhM;
        this.empruntManager = empM;
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        // L'administrateur est déconnecté, on charge la vue de connexion admin
        loadAuthView("/com/sgeb/sgbd/view/Welcome.fxml", event);
    }

    // 🔑 Méthode utilitaire pour changer la vue sur la fenêtre principale
    // (identique à celle de l'adhérent)
    // Dans AdminPasswordController.java
    private void loadAuthView(String fxmlPath, ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // --- 1. Vérification de la ressource FXML (pour éviter "Location is not set")
            // ---
            java.net.URL location = getClass().getResource(fxmlPath);
            if (location == null) {
                System.err.println("ERREUR FXML: Fichier introuvable à : " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();
            Object controller = loader.getController();

            // --- 2. Ré-injection des Managers dans le contrôleur cible (WelcomeController)
            // ---
            if (controller instanceof ManagerLoader) {
                ManagerLoader managerController = (ManagerLoader) controller;
                managerController.setManagers(this.documentManager, this.adherentManager, this.empruntManager);
            }

            stage.getScene().setRoot(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Échec du chargement du FXML : " + fxmlPath);
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        String ancienMdpSaisi = ancienPasswordField.getText();
        String nouveauMdp = nouveauPasswordField.getText();
        String confirmationMdp = confirmerPasswordField.getText();

        messageLabel.setText("");

        if (ancienMdpSaisi.isEmpty() || nouveauMdp.isEmpty() || confirmationMdp.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (!nouveauMdp.equals(confirmationMdp)) {
            messageLabel.setText("Les nouveaux mots de passe ne correspondent pas.");
            return;
        }

        try {
            // 1. Charger le hash actuel pour vérification
            this.hashMotDePasseActuel = loadAdminHash();
            if (this.hashMotDePasseActuel == null) {
                messageLabel.setText("Erreur: Configuration administrateur non trouvée.");
                return;
            }

            // 2. Vérification de l'ancien mot de passe
            if (!PasswordUtil.checkPassword(ancienMdpSaisi, hashMotDePasseActuel)) {
                messageLabel.setText("L'ancien mot de passe est incorrect.");
                return;
            }

            // 3. Hachage et mise à jour
            String nouveauHash = PasswordUtil.hashPassword(nouveauMdp);

            // 🔑 Appel à la méthode de sauvegarde
            saveAdminCredentials(ADMIN_IDENTIFIER, nouveauHash);

            messageLabel.setText("Mot de passe changé avec succès !");

            // Nettoyage des champs
            ancienPasswordField.clear();
            nouveauPasswordField.clear();
            confirmerPasswordField.clear();

        } catch (IOException e) {
            System.err.println("Erreur de fichier lors du changement de mot de passe : " + e.getMessage());
            messageLabel.setText("Erreur système lors de la mise à jour.");
        }
    }

    // --- Méthodes de Gestion de Fichier ---

    private String loadAdminHash() throws IOException {
        Properties props = new Properties();
        File configFile = new File(ADMIN_CONFIG_FILE);

        if (!configFile.exists()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            // On s'assure de lire la bonne propriété
            return props.getProperty("admin.hash");
        }
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