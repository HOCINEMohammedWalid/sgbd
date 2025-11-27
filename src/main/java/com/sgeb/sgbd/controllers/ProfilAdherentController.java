package com.sgeb.sgbd.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.event.ActionEvent; // Pour la méthode handleDeconnexion

import javafx.stage.Stage; // Pour la fenêtre principale

import java.io.IOException;

import com.sgeb.sgbd.model.*;

public class ProfilAdherentController implements ManagerLoader {

    // --- Éléments FXML ---
    @FXML
    private VBox mainVBox;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField adresseField;
    @FXML
    private TextField telephoneField;
    @FXML
    private Label messageLabel;

    // Éléments spécifiques au mode modification
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label passwordLabel;
    @FXML
    private Button modifierButton;
    @FXML
    private Button sauvegarderButton;

    // --- Données et Managers ---
    private AdherentManager adherentManager;
    private Adherent adherentConnecte;
    private String hashMotDePasseActuel; // Stocke le mot de passe HACHÉ pour la session
    private DocumentManager documentManager;
    private EmpruntManager empruntManager;

    public void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM) {
        this.documentManager = docM;
        this.adherentManager = adhM;
        this.empruntManager = empM;
    }
    // --- Initialisation ---

    // Méthode appelée après le chargement du FXML (étape 1)
    @FXML
    public void initialize() {
        setEditableMode(false); // Commence en mode Affichage
    }

    public void initData(AdherentManager manager, Adherent adherent, String hashMdp) {
        this.adherentConnecte = adherent;
        this.hashMotDePasseActuel = hashMdp;

        if (adherentConnecte != null) {

            afficherDetailsAdherent();

            // Initialiser l'état (Affichage par défaut)
            setEditableMode(false);
        } else {
            messageLabel.setText("Erreur : Utilisateur non connecté.");
        }
    }

    private void afficherDetailsAdherent() {
        // L'email sert d'identifiant et est affiché
        emailField.setText(adherentConnecte.getEmail());
        nomField.setText(adherentConnecte.getNom());
        prenomField.setText(adherentConnecte.getPrenom());
        adresseField.setText(adherentConnecte.getAdresse());
        telephoneField.setText(adherentConnecte.getTelephone());

        passwordField.clear();
    }

    // --- Gestion du Mode d'Édition ---

    /**
     * Action pour passer en mode modification.
     */
    @FXML
    private void handleModifierAction() {
        setEditableMode(true);
    }

    /**
     * Définit l'état d'édition des champs et des boutons.
     * 
     * @param edit Si true, passe en mode Modification; sinon, en mode Affichage.
     */
    private void setEditableMode(boolean edit) {
        // Champs d'information modifiables
        nomField.setEditable(edit);
        prenomField.setEditable(edit);
        adresseField.setEditable(edit);
        telephoneField.setEditable(edit);

        // L'email (identifiant) est toujours non modifiable
        emailField.setEditable(false);

        // Champ Mot de passe (seulement visible/utilisable en mode modification)
        passwordField.setVisible(edit);
        passwordField.setManaged(edit);
        passwordLabel.setVisible(edit);
        passwordLabel.setManaged(edit);
        passwordField.clear(); // Toujours effacer le champ au changement de mode

        // Boutons
        modifierButton.setVisible(!edit);
        modifierButton.setManaged(!edit);

        sauvegarderButton.setVisible(edit);
        sauvegarderButton.setManaged(edit);

        // Effacer les messages
        messageLabel.setText("");
        messageLabel.setStyle("");
    }

    // --- Logique de Sauvegarde ---

    @FXML
    private void sauvegarderProfil() {
        // 1. Récupérer les nouvelles valeurs
        String nouveauNom = nomField.getText();
        String nouveauPrenom = prenomField.getText();
        String nouveauMotDePasse = passwordField.getText();
        String nouvelleAdresse = adresseField.getText();
        String nouveauTelephone = telephoneField.getText();

        // 2. Mettre à jour l'objet Adherent (champs non-sécurité)
        adherentConnecte.setNom(nouveauNom);
        adherentConnecte.setPrenom(nouveauPrenom);
        adherentConnecte.setAdresse(nouvelleAdresse);
        adherentConnecte.setTelephone(nouveauTelephone);

        // 3. Préparer le mot de passe à envoyer au Manager
        String nouveauHash = null;
        if (!nouveauMotDePasse.trim().isEmpty()) {

            nouveauHash = nouveauMotDePasse; // Remplacement temporaire pour compilation
        }

        // 4. Sauvegarder dans la base de données via le Manager
        try {
            // Appel de la méthode spéciale qui met à jour l'Adherent ET le mot de passe
            // séparément
            boolean success = adherentManager.updateAdherentProfil(adherentConnecte, nouveauHash);

            if (success) {
                messageLabel.setText("Profil mis à jour avec succès !");
                messageLabel.setStyle("-fx-text-fill: green;");
                passwordField.clear();

                // Mettre à jour le hash stocké si le mot de passe a été changé
                if (nouveauHash != null) {
                    hashMotDePasseActuel = nouveauHash;
                }

                // Revenir en mode Affichage
                setEditableMode(false);

            } else {
                messageLabel.setText("Échec de la mise à jour du profil.");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            messageLabel.setText("Erreur système lors de la sauvegarde : " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // L'adhérent est déconnecté, on charge la vue de connexion adhérent
        loadAuthView("/com/sgeb/sgbd/view/Welcome.fxml", event);
    }

    // 🔑 Méthode utilitaire pour changer la vue sur la fenêtre principale
    // Dans ProfilAdherentController.java
    private void loadAuthView(String fxmlPath, ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // --- 1. Vérification de la ressource FXML ---
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
                System.out.println(this.documentManager);
                managerController.setManagers(this.documentManager, this.adherentManager, this.empruntManager);
            }

            stage.getScene().setRoot(root);
            stage.show();

            // Nettoyage des données de session (Important)
            this.adherentConnecte = null;
            this.hashMotDePasseActuel = null;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Échec du chargement du FXML :" + fxmlPath);
        }
    }
}