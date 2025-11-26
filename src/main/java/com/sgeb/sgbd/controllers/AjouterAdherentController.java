package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.enums.StatutAdherent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern; // Import nécessaire pour Regex

public class AjouterAdherentController {

    // Dépendances Managér
    private AdherentManager adherentManager;
    // J'ai corrigé le type du contrôleur parent pour correspondre à votre code
    // précédent (AdherentController)
    // S'il s'appelle bien AdherentsController, gardez-le. J'utilise
    // AdherentController ici par défaut.
    private AdherentsController parentController;

    // ==== FXML Fields ====
    @FXML
    private TextArea adress;
    @FXML
    private TextField email;
    @FXML
    private TextField nom;
    @FXML
    private TextField prenom;
    @FXML
    private TextField tel;

    // =============================================
    // REGEX POUR LA VALIDATION DES CHAMPS
    // =============================================

    // Regex pour vérifier qu'un nom/prénom ne contient que des lettres, tirets, et
    // espaces
    private static final String NAME_REGEX = "^[\\p{L} .'-]+$";
    // Regex simple pour email (assez robuste sans être trop restrictif)
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    // Regex pour numéro de téléphone français (0X XX XX XX XX ou +33 X XX XX XX XX)
    private static final String TEL_REGEX = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$";

    // =============================================
    // INJECTION DES DÉPENDANCES
    // =============================================

    /**
     * Définit le Manager pour l'enregistrement.
     * * @param manager L'AdherentManager.
     */
    public void setManager(AdherentManager manager) {
        this.adherentManager = manager;
    }

    /**
     * Définit le contrôleur parent pour rafraîchissement après succès.
     * * @param parentController Le AdherentController.
     */
    public void setParentController(AdherentsController parentController) {
        this.parentController = parentController;
    }

    // =============================================
    // ACTION D'AJOUT AVEC VALIDATION
    // =============================================

    @FXML
    void ajouter(ActionEvent event) {
        // 1. Validation de la dépendance
        if (adherentManager == null) {
            showAlert("Erreur d'Initialisation", "Le Manager d'Adhérents n'a pas été initialisé.",
                    Alert.AlertType.ERROR);
            return;
        }

        // 2. Récupération des données
        String nomStr = nom.getText().trim();
        String prenomStr = prenom.getText().trim();
        String emailStr = email.getText().trim();
        String telStr = tel.getText().trim();
        String adressStr = adress.getText().trim();

        // 3. Validation des champs obligatoires
        if (nomStr.isEmpty() || prenomStr.isEmpty() || emailStr.isEmpty() || telStr.isEmpty()) {
            showAlert("Champs Incomplets",
                    "Veuillez remplir tous les champs obligatoires (Nom, Prénom, Email, Téléphone).",
                    Alert.AlertType.WARNING);
            return;
        }

        // 4. Validation du format des données avec Regex
        if (!validate(nomStr, NAME_REGEX)) {
            showAlert("Erreur de Format", "Le Nom contient des caractères invalides.", Alert.AlertType.WARNING);
            return;
        }
        if (!validate(prenomStr, NAME_REGEX)) {
            showAlert("Erreur de Format", "Le Prénom contient des caractères invalides.", Alert.AlertType.WARNING);
            return;
        }
        if (!validate(emailStr, EMAIL_REGEX)) {
            showAlert("Erreur de Format", "Le format de l'Email est invalide.", Alert.AlertType.WARNING);
            return;
        }
        // Note: La validation du numéro de téléphone est stricte et peut nécessiter un
        // ajustement.
        if (!validate(telStr, TEL_REGEX)) {
            showAlert("Erreur de Format", "Le format du Téléphone est invalide (ex: 06 12 34 56 78).",
                    Alert.AlertType.WARNING);
            return;
        }

        // 5. Création de l'objet Adherent
        Adherent nouvelAdherent = new Adherent(
                0, // ID temporaire
                nomStr,
                prenomStr,
                emailStr,
                adressStr,
                telStr,
                LocalDate.now(), // Date d'inscription
                StatutAdherent.ACTIF,
                new ArrayList<>() // Liste d'emprunts
        );

        // 6. Tentative d'enregistrement via le Manager
        try {
            adherentManager.ajouterAdherent(nouvelAdherent);

            // Succès
            showAlert("Succès", "L'adhérent " + nomStr + " " + prenomStr + " a été ajouté.",
                    Alert.AlertType.INFORMATION);

            // 7. Fermeture de la fenêtre et rafraîchissement
            if (parentController != null) {
                parentController.refreshTable(); // Rafraîchit la table parente
            }

            closeWindow();

        } catch (SQLException e) {
            showAlert("Erreur Base de Données",
                    "Erreur lors de l'enregistrement de l'adhérent. Vérifiez les doublons (ex: Email) : "
                            + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            showAlert("Erreur de Validation", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // =============================================
    // UTILS
    // =============================================

    /**
     * Vérifie si la chaîne de caractères correspond à l'expression régulière
     * fournie.
     * 
     * @param value La chaîne à vérifier.
     * @param regex L'expression régulière.
     * @return true si la valeur est valide, false sinon.
     */
    private boolean validate(String value, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nom.getScene().getWindow();
        stage.close();
    }
}