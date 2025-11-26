package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;
import com.sgeb.sgbd.model.Livre;
import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.exception.EmpruntException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DetailsLivreControllerNonAdmin implements Initializable, DetailsControllerBase {

    // Manager pour l'action d'emprunt et le Document
    private EmpruntManager empruntManager;
    private Livre livre;
    private Adherent adherent;

    @Override
    public void setAdherent(Adherent adherent) {
        this.adherent = adherent;
    }

    // ==== FXML Fields (tous en TextField pour la lecture seule, sauf TextArea)
    // ====
    @FXML
    private Button Anuller;
    @FXML
    private TextField Auteurs;
    @FXML
    private Button Emprunte;
    @FXML
    private TextField annee_p;
    @FXML
    private TextField catg;
    @FXML
    private TextField collec;
    @FXML
    private TextField editeur;
    @FXML
    private TextField isbn;
    @FXML
    private TextField langue;
    @FXML
    private TextField mot_cles;
    @FXML
    private TextField nb_pages; // Renommé de nbpages à nb_pages pour correspondre au FXML
    @FXML
    private TextArea resume;
    @FXML
    private TextField titre;

    // =============================================
    // INJECTION DU DOCUMENT ET MANAGER
    // =============================================
    @Override
    public void setEmpruntManager(EmpruntManager empruntManager) {
        this.empruntManager = empruntManager;

    }

    @Override
    public void setDocument(Document document) {

        if (document instanceof Livre) {
            this.livre = (Livre) document;
            initializeFields();
        } else {
            System.err.println("Erreur: Le document n'est pas un Livre.");
        }
    }

    /** Implémentation requise par DetailsControllerBase, mais laissée vide. */
    @Override
    public void setDocumentManager(DocumentManager documentManager) {
        // Non nécessaire pour le non-admin
    }

    /** Implémentation requise par DetailsControllerBase, mais laissée vide. */
    @Override
    public void setParent(DocumentsController table) {
        // Non nécessaire pour le non-admin
    }

    // =============================================
    // INITIALISATION DES CHAMPS
    // =============================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Rendre tous les champs non modifiables
        setFieldsReadOnly(true);
    }

    private void setFieldsReadOnly(boolean readOnly) {
        // Rend tous les TextField et TextArea en lecture seule
        if (Auteurs != null)
            Auteurs.setEditable(!readOnly);
        if (annee_p != null)
            annee_p.setEditable(!readOnly);
        if (catg != null)
            catg.setEditable(!readOnly);
        if (collec != null)
            collec.setEditable(!readOnly);
        if (editeur != null)
            editeur.setEditable(!readOnly);
        if (isbn != null)
            isbn.setEditable(!readOnly);
        if (langue != null)
            langue.setEditable(!readOnly);
        if (mot_cles != null)
            mot_cles.setEditable(!readOnly);
        if (nb_pages != null)
            nb_pages.setEditable(!readOnly);
        if (resume != null)
            resume.setEditable(!readOnly);
        if (titre != null)
            titre.setEditable(!readOnly);
    }

    private void initializeFields() {
        if (this.livre != null) {
            // Champs communs/Document
            if (titre != null)
                titre.setText(livre.getTitre());
            if (editeur != null)
                editeur.setText(livre.getEditeur());
            if (resume != null)
                resume.setText(livre.getResume());

            // Listes converties en String
            String auteursStr = String.join(", ", livre.getAuteurs());
            if (Auteurs != null)
                Auteurs.setText(auteursStr);
            String motsClesStr = String.join(", ", livre.getMotsCles());
            if (mot_cles != null)
                mot_cles.setText(motsClesStr);

            // Enums et Entiers
            if (annee_p != null)
                annee_p.setText(String.valueOf(livre.getAnneePublication()));
            if (langue != null)
                langue.setText(livre.getLangue());
            if (catg != null)
                catg.setText(livre.getCategorie().toString());

            // Champs spécifiques à Livre
            if (isbn != null)
                isbn.setText(livre.getISBN());
            if (collec != null)
                collec.setText(livre.getCollection());

            if (nb_pages != null)
                nb_pages.setText(String.valueOf(livre.getNbPages()));
        }
    }

    // =============================================
    // ACTIONS UTILISATEUR
    // =============================================

    @FXML
    void anuller(ActionEvent event) {
        // Récupérer la Stage (fenêtre) actuelle via le bouton Anuller
        if (Anuller != null && Anuller.getScene() != null) {
            Stage stage = (Stage) Anuller.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void emprunter(ActionEvent event) {
        // Validation que les objets nécessaires sont présents
        if (empruntManager == null || livre == null || adherent == null) {
            showAlert("Erreur d'initialisation",
                    "L'Article, l'Adhérent ou le Manager d'emprunt est manquant.",
                    Alert.AlertType.WARNING);
            return;
        }
        if (!adherent.peutEmprunter()) {
            showAlert("Erreur",
                    "l'Adhérent ne peut pas emprunter.",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            // Tentative d'effectuer l'emprunt
            empruntManager.emprunter(livre, adherent);

            // Succès : Afficher le message de réussite
            showAlert("Emprunt réussi",
                    "Confirmation",
                    "L'article \"" + livre.getTitre() + "\" a été emprunté avec succès par " + adherent.getNom()
                            + ".",
                    Alert.AlertType.INFORMATION);

            // Fermer la fenêtre de détails après un emprunt réussi
            anuller(null);

        } catch (EmpruntException e) {
            // Gestion des erreurs spécifiques à l'emprunt (ex: déjà emprunté, quota
            // dépassé)
            showAlert("Échec de l'emprunt",
                    "Règle non respectée",
                    "Impossible d'emprunter l'article : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();

        } catch (SQLException e) {
            // Gestion des erreurs de base de données
            showAlert("Erreur Base de Données",
                    "Erreur lors de la sauvegarde de l'emprunt.",
                    "Une erreur de base de données est survenue : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // =============================================
    // UTILS (Correction pour supporter le header et le content)
    // =============================================
    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Surcharge pour la compatibilité avec l'ancienne signature (sans header)
    private void showAlert(String title, String content, Alert.AlertType type) {
        showAlert(title, null, content, type);
    }
}