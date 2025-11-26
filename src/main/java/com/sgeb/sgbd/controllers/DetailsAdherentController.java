package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.enums.StatutAdherent;
import com.sgeb.sgbd.model.exception.AdherentInexistantException;
import com.sgeb.sgbd.model.exception.ModificationAdherentException;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 1. Définition de l'interface (pour la communication avec le contrôleur parent)
// Assurez-vous d'avoir une telle interface ou utilisez cette classe directement.
// J'utilise le nom 'AdherentDetailsControllerBase' comme référence de l'autre réponse.
public class DetailsAdherentController implements Initializable { // Note: 'Deatails' devrait être 'Details'

    private Adherent adherent;
    private AdherentManager adherentManager;
    private AdherentsController parentController;

    // Format de date pour l'affichage
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // REGEX pour la validation (réutilisées de AjouterAdherentController)
    private static final String NAME_REGEX = "^[\\p{L} .'-]+$";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final String TEL_REGEX = "^(?:(?:\\+|00)33|0)\\s*[1-9](?:[\\s.-]*\\d{2}){4}$";

    // ==== FXML Fields ====
    @FXML
    private Button Anuller;

    @FXML
    private Button Update;
    @FXML
    private TextArea adress;
    @FXML
    private TextField date; // Date d'inscription
    @FXML
    private TextField email;
    @FXML
    private TextField nom;
    @FXML
    private TextField prenom;
    @FXML
    private ChoiceBox<StatutAdherent> status; // Changé le type générique à StatutAdherent
    @FXML
    private TextField tel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation de la ChoiceBox
        status.setItems(FXCollections.observableArrayList(Arrays.asList(StatutAdherent.values())));

        // La date d'inscription est non modifiable
        date.setEditable(false);
    }

    // =============================================
    // INJECTION DES DONNÉES
    // =============================================

    /** Définit l'adhérent à afficher/modifier. */
    public void setAdherent(Adherent adherent) {
        this.adherent = adherent;
        if (adherent != null) {
            chargerDetails();
        }
    }

    /** Définit le Manager pour l'enregistrement des modifications. */
    public void setAdherentManager(AdherentManager manager) {
        this.adherentManager = manager;
    }

    /** Définit le contrôleur parent pour le rafraîchissement de la table. */
    public void setParent(AdherentsController parent) {
        this.parentController = parent;
    }

    // =============================================
    // CHARGEMENT DES DONNÉES
    // =============================================

    private void chargerDetails() {
        nom.setText(adherent.getNom());
        prenom.setText(adherent.getPrenom());
        email.setText(adherent.getEmail());
        tel.setText(adherent.getTelephone());
        adress.setText(adherent.getAdresse());

        // Affichage de la date au format lisible
        if (adherent.getDateInscription() != null) {
            date.setText(adherent.getDateInscription().format(DATE_FORMATTER));
        } else {
            date.setText("");
        }

        // Sélection du statut actuel
        status.getSelectionModel().select(adherent.getStatut());
    }

    // =============================================
    // ACTION DE MODIFICATION
    // =============================================

    /**
     * Cette méthode devrait être renommée en 'modifier' ou 'enregistrer'
     * dans le FXML, car elle ne fait pas d'ajout.
     */
    @FXML
    void update(ActionEvent event) {
        if (adherent == null || adherentManager == null) {
            showAlert("Erreur", "L'adhérent ou le Manager n'est pas initialisé.", Alert.AlertType.ERROR);
            return;
        }

        // 1. Récupération et Validation des données
        String nomStr = nom.getText().trim();
        String prenomStr = prenom.getText().trim();
        String emailStr = email.getText().trim();
        String telStr = tel.getText().trim();
        String adressStr = adress.getText().trim();
        StatutAdherent statutSelect = status.getSelectionModel().getSelectedItem();

        // Validation des champs obligatoires
        if (nomStr.isEmpty() || prenomStr.isEmpty() || emailStr.isEmpty() || telStr.isEmpty() || statutSelect == null) {
            showAlert("Champs Incomplets", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.WARNING);
            return;
        }

        // Validation du format avec Regex
        if (!validate(nomStr, NAME_REGEX) || !validate(prenomStr, NAME_REGEX)) {
            showAlert("Erreur de Format", "Le Nom ou le Prénom contient des caractères invalides.",
                    Alert.AlertType.WARNING);
            return;
        }
        if (!validate(emailStr, EMAIL_REGEX)) {
            showAlert("Erreur de Format", "Le format de l'Email est invalide.", Alert.AlertType.WARNING);
            return;
        }
        if (!validate(telStr, TEL_REGEX)) {
            showAlert("Erreur de Format", "Le format du Téléphone est invalide (ex: 06 12 34 56 78).",
                    Alert.AlertType.WARNING);
            return;
        }

        // 3. Tentative d'enregistrement via le Manager
        try {
            // Assurez-vous que votre Manager a une méthode 'modifierAdherent' (ou 'update')

            adherentManager.modifierAdherent(adherent.getIdAdherent(), adherent -> {// 2. Mise à jour de l'objet
                                                                                    // Adherent
                adherent.setNom(nomStr);
                adherent.setPrenom(prenomStr);
                adherent.setEmail(emailStr);
                adherent.setTelephone(telStr);
                adherent.setAdresse(adressStr);
                adherent.setStatut(statutSelect);
            });
            showAlert("Succès", "L'adhérent a été modifié.", Alert.AlertType.INFORMATION);

            // 4. Rafraîchissement et fermeture
            if (parentController != null) {
                parentController.refreshTable();
            }
            closeWindow();

        } catch (AdherentInexistantException e) {
            showAlert("Erreur", "L'adhérent spécifié est introuvable dans la base de données.", Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (ModificationAdherentException e) {
            // Capture des erreurs métier spécifiques au Manager (si implémenté)
            showAlert("Erreur Métier", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (SQLException e) {
            // Capture des erreurs de la base de données (connexion, contrainte unique,
            // etc.)
            showAlert("Erreur Base de Données",
                    "Erreur SQL lors de la modification : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            // Capture toute autre exception inattendue
            showAlert("Erreur Générale", "Une erreur inattendue est survenue : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // =============================================
    // UTILS
    // =============================================

    /**
     * Vérifie si la chaîne de caractères correspond à l'expression régulière
     * fournie.
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

    @FXML
    void anuller(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nom.getScene().getWindow();
        stage.close();
    }
}