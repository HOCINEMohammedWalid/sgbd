package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;
import com.sgeb.sgbd.model.exception.EmpruntException;
import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.DVD;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DetailsDVDControllerNonAdmin implements Initializable, DetailsControllerBase {

    // Manager pour l'action d'emprunt et le Document
    private EmpruntManager empruntManager;
    private DVD dvd;
    private Adherent adherent;

    @Override
    public void setAdherent(Adherent adherent) {
        this.adherent = adherent;
    }

    // ==== FXML Fields (lecture seule) ====
    @FXML
    private Button Anuller;
    @FXML
    private TextField Auteurs;
    @FXML
    private Button Emprunte;
    @FXML
    private TextField annee_p;
    @FXML
    private TextField classification;
    @FXML
    private TextField duree;
    @FXML
    private TextField editeur;
    @FXML
    private TextField langue;
    @FXML
    private TextField mot_cles;
    @FXML
    private TextField realisateur;
    @FXML
    private TextArea resume;
    @FXML
    private TextField titre;

    @FXML
    private TextField catg;

    // =============================================
    // INJECTION DU DOCUMENT ET MANAGER
    // =============================================

    public void setEmpruntManager(EmpruntManager empruntManager) {
        this.empruntManager = empruntManager;
    }

    @Override
    public void setDocument(Document document) {
        if (document instanceof DVD) {
            this.dvd = (DVD) document;
            initializeFields();
        } else {
            System.err.println("Erreur: Le document n'est pas un DVD.");
        }
    }

    // Non utilisés dans le contrôleur non-admin mais requis par l'interface
    @Override
    public void setDocumentManager(DocumentManager documentManager) {
        /* vide */ }

    @Override
    public void setParent(DocumentsController table) {
        /* vide */ }

    // =============================================
    // INITIALISATION DES CHAMPS
    // =============================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Rendre tous les champs non modifiables au démarrage
        setFieldsReadOnly(true);
    }

    private void setFieldsReadOnly(boolean readOnly) {
        // Rend tous les TextFields et TextArea non éditables
        if (Auteurs != null)
            Auteurs.setEditable(!readOnly);
        if (annee_p != null)
            annee_p.setEditable(!readOnly);
        if (classification != null)
            classification.setEditable(!readOnly);
        if (duree != null)
            duree.setEditable(!readOnly);
        if (editeur != null)
            editeur.setEditable(!readOnly);
        if (langue != null)
            langue.setEditable(!readOnly);
        if (mot_cles != null)
            mot_cles.setEditable(!readOnly);
        if (realisateur != null)
            realisateur.setEditable(!readOnly);
        if (resume != null)
            resume.setEditable(!readOnly);
        if (titre != null)
            titre.setEditable(!readOnly);
        if (catg != null)
            catg.setEditable(!readOnly);
    }

    private void initializeFields() {
        if (this.dvd != null) {
            // Champs communs/Document
            if (titre != null)
                titre.setText(dvd.getTitre());
            if (editeur != null)
                editeur.setText(dvd.getEditeur());
            if (resume != null)
                resume.setText(dvd.getResume());

            // Listes converties en String
            if (Auteurs != null)
                Auteurs.setText(String.join(", ", dvd.getAuteurs()));
            if (mot_cles != null)
                mot_cles.setText(String.join(", ", dvd.getMotsCles()));

            // Enums et Entiers
            if (annee_p != null)
                annee_p.setText(String.valueOf(dvd.getAnneePublication()));
            if (langue != null)
                langue.setText(dvd.getLangue());
            if (catg != null)
                catg.setText(dvd.getCategorie().toString());

            // Champs spécifiques au DVD
            if (realisateur != null)
                realisateur.setText(dvd.getRealisateur());
            if (classification != null)
                classification.setText(dvd.getClassification());
            if (duree != null)
                duree.setText(String.valueOf(dvd.getDuree()));
        }
    }

    // =============================================
    // ACTIONS UTILISATEUR
    // =============================================

    @FXML
    void anuller(ActionEvent event) {
        // Ferme la fenêtre (Stage) actuelle
        if (Anuller != null && Anuller.getScene() != null) {
            Stage stage = (Stage) Anuller.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void emprunter(ActionEvent event) {
        // Validation que les objets nécessaires sont présents
        if (empruntManager == null || dvd == null || adherent == null) {
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
            empruntManager.emprunter(dvd, adherent);

            // Succès : Afficher le message de réussite
            showAlert("Emprunt réussi",
                    "Confirmation",
                    "L'article \"" + dvd.getTitre() + "\" a été emprunté avec succès par " + adherent.getNom()
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