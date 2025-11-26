package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;
import com.sgeb.sgbd.model.EBook;
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

public class DetailsEbookControllerNonAdmin implements Initializable, DetailsControllerBase {

    // Manager pour l'action d'emprunt et le Document
    private EmpruntManager empruntManager;
    private EBook ebook;
    private Adherent adherent;

    @Override
    public void setAdherent(Adherent adherent) {
        this.adherent = adherent;
    }

    // ==== FXML Fields (adaptés pour la lecture seule) ====
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
    private TextField doi; // Utilisé pour le format/type
    @FXML
    private CheckBox drm; // Reste un CheckBox pour l'affichage de l'état
    @FXML
    private TextField editeur;
    @FXML
    private TextField langue;
    @FXML
    private TextField mot_cles;
    @FXML
    private TextArea resume;
    @FXML
    private TextField titre;
    @FXML
    private TextField url;

    // =============================================
    // INJECTION DU DOCUMENT ET MANAGER
    // =============================================

    public void setEmpruntManager(EmpruntManager empruntManager) {
        this.empruntManager = empruntManager;
    }

    @Override
    public void setDocument(Document document) {
        if (document instanceof EBook) {
            this.ebook = (EBook) document;
            initializeFields();
        } else {
            System.err.println("Erreur: Le document n'est pas un Ebook.");
        }
    }

    /** Implémentation requise par DetailsControllerBase (non utilisée ici). */
    @Override
    public void setDocumentManager(DocumentManager documentManager) {
        // Laisser vide
    }

    /** Implémentation requise par DetailsControllerBase (non utilisée ici). */
    @Override
    public void setParent(DocumentsController table) {
        // Laisser vide
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
        // Rendre les TextFields et TextArea non éditables
        if (Auteurs != null)
            Auteurs.setEditable(!readOnly);
        if (annee_p != null)
            annee_p.setEditable(!readOnly);
        if (catg != null)
            catg.setEditable(!readOnly);
        if (doi != null)
            doi.setEditable(!readOnly);
        if (editeur != null)
            editeur.setEditable(!readOnly);
        if (langue != null)
            langue.setEditable(!readOnly);
        if (mot_cles != null)
            mot_cles.setEditable(!readOnly);
        if (resume != null)
            resume.setEditable(!readOnly);
        if (titre != null)
            titre.setEditable(!readOnly);
        if (url != null)
            url.setEditable(!readOnly);

        // Désactiver le CheckBox pour empêcher la modification
        if (drm != null)
            drm.setDisable(readOnly);
    }

    private void initializeFields() {
        if (this.ebook != null) {
            // Champs communs/Document
            if (titre != null)
                titre.setText(ebook.getTitre());
            if (editeur != null)
                editeur.setText(ebook.getEditeur());
            if (resume != null)
                resume.setText(ebook.getResume());

            // Listes converties en String
            if (Auteurs != null)
                Auteurs.setText(String.join(", ", ebook.getAuteurs()));
            if (mot_cles != null)
                mot_cles.setText(String.join(", ", ebook.getMotsCles()));

            // Enums et Entiers
            if (annee_p != null)
                annee_p.setText(String.valueOf(ebook.getAnneePublication()));
            if (langue != null)
                langue.setText(ebook.getLangue());
            if (catg != null)
                catg.setText(ebook.getCategorie().toString());

            // Champs spécifiques à Ebook
            if (url != null)
                url.setText(ebook.getUrlAcces());
            if (doi != null)
                doi.setText(ebook.getFormat()); // Le champ 'doi' est utilisé pour le format

            if (drm != null)
                drm.setSelected(ebook.hasDrm());
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
        if (empruntManager == null || ebook == null || adherent == null) {
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
            empruntManager.emprunter(ebook, adherent);

            // Succès : Afficher le message de réussite
            showAlert("Emprunt réussi",
                    "Confirmation",
                    "L'article \"" + ebook.getTitre() + "\" a été emprunté avec succès par " + adherent.getNom()
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