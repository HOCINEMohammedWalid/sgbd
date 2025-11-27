package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;
import com.sgeb.sgbd.model.These;
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

public class DetailsTheseNonAdminController implements Initializable, DetailsControllerBase {

    // Manager pour l'action d'emprunt
    private EmpruntManager empruntManager;
    private These these;
    private Adherent adherent;

    @Override
    public void setAdherent(Adherent adherent) {
        this.adherent = adherent;
    }

    // Champs FXML (réutilisation de votre squelette)
    @FXML
    private Button Anuller;
    @FXML
    private TextField Auteurs;
    @FXML
    private Button Emprunter;
    @FXML
    private Spinner<Integer> annee_p; // Typé en Integer
    @FXML
    private TextField auteur_p;
    @FXML
    private TextField catg;
    @FXML
    private TextField date_s;
    @FXML
    private TextField discp;
    @FXML
    private TextField dr;
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
    private TextField tp_acees;
    @FXML
    private TextField univ;

    // =============================================
    // INJECTION DU DOCUMENT ET MANAGER
    // =============================================

    public void setEmpruntManager(EmpruntManager empruntManager) {
        this.empruntManager = empruntManager;
    }

    @Override
    public void setDocument(Document document) {
        if (document instanceof These) {
            this.these = (These) document;
            initializeFields();
        } else {
            // Gérer l'erreur si le type de document n'est pas These
            System.err.println("Erreur: Le document n'est pas une Thèse.");
        }
    }

    /**
     * N'est pas utilisé dans le contrôleur Non-Admin mais doit être implémenté
     * car l'interface DetailsControllerBase le requiert.
     */
    @Override
    public void setDocumentManager(DocumentManager documentManager) {
        // Laisser vide
    }

    /**
     * N'est pas utilisé dans le contrôleur Non-Admin mais doit être implémenté
     * car l'interface DetailsControllerBase le requiert.
     */
    @Override
    public void setParent(DocumentsController table) {
        // Laisser vide
    }

    // =============================================
    // INITIALISATION DES CHAMPS
    // =============================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (annee_p != null) {
            annee_p.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1800, 2100, 2025));
        }

        setFieldsReadOnly(true);
    }

    private void setFieldsReadOnly(boolean readOnly) {
        // L'utilisation des opérateurs ternaires et vérifications nulles rend le code
        // plus sûr
        // L'utilisation de setDisable(readOnly) est préférable pour les contrôles de
        // sélection/saisie
        if (Auteurs != null)
            Auteurs.setEditable(!readOnly);
        if (auteur_p != null)
            auteur_p.setEditable(!readOnly);
        if (date_s != null)
            date_s.setEditable(!readOnly);
        if (discp != null)
            discp.setEditable(!readOnly);
        if (dr != null)
            dr.setEditable(!readOnly);
        if (editeur != null)
            editeur.setEditable(!readOnly);
        if (mot_cles != null)
            mot_cles.setEditable(!readOnly);
        if (resume != null)
            resume.setEditable(!readOnly);
        if (titre != null)
            titre.setEditable(!readOnly);
        if (tp_acees != null)
            tp_acees.setEditable(!readOnly);
        if (univ != null)
            univ.setEditable(!readOnly);

        // Pour les ChoiceBox et Spinner, désactiver est la bonne approche
        if (annee_p != null)
            annee_p.setDisable(readOnly);
        if (catg != null)
            catg.setDisable(readOnly);
        if (langue != null)
            langue.setDisable(readOnly);
    }

    private void initializeFields() {
        if (this.these != null) {
            // Champs hérités de Document
            if (titre != null)
                titre.setText(these.getTitre());
            if (resume != null)
                resume.setText(these.getResume());
            if (editeur != null)
                editeur.setText(these.getEditeur());

            // Conversion de List<String>
            String auteursStr = String.join(", ", these.getAuteurs());
            if (Auteurs != null)
                Auteurs.setText(auteursStr);

            // Conversion de List<String>
            String motsClesStr = String.join(", ", these.getMotsCles());
            if (mot_cles != null)
                mot_cles.setText(motsClesStr);

            // Initialisation du Spinner (si annee_p est un Spinner<Integer>)
            if (annee_p != null && annee_p.getValueFactory() != null) {
                annee_p.getValueFactory().setValue(these.getAnneePublication());
            }

            // Champs spécifiques à These
            if (auteur_p != null)
                auteur_p.setText(these.getAuteurPrincipal());
            if (dr != null)
                dr.setText(these.getDirecteurRecherche());
            if (univ != null)
                univ.setText(these.getUniversite());
            if (discp != null)
                discp.setText(these.getDiscipline());

            // Affichage de LocalDate
            if (date_s != null)
                date_s.setText(these.getDateSoutenance() != null ? these.getDateSoutenance().toString() : "");

            if (tp_acees != null)
                tp_acees.setText(these.getTypeAcces());

            if (langue != null) {
                langue.setText(these.getLangue());
            }
            if (catg != null) {
                catg.setText(these.getCategorie().toString());
            }
        }
    }

    // =============================================
    // ACTIONS UTILISATEUR
    // =============================================
    DocumentsControllerNonAdmin documentsControllerNonAdmin;

    public void setParentN(DocumentsControllerNonAdmin d) {
        documentsControllerNonAdmin = d;
    }

    @FXML
    void emprunter(ActionEvent event) {
        // Validation que les objets nécessaires sont présents
        if (empruntManager == null || these == null || adherent == null) {
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
            empruntManager.emprunter(these, adherent);

            // Succès : Afficher le message de réussite
            showAlert("Emprunt réussi",
                    "Confirmation",
                    "L'article \"" + these.getTitre() + "\" a été emprunté avec succès par " + adherent.getNom()
                            + ".",
                    Alert.AlertType.INFORMATION);
            documentsControllerNonAdmin.refreshTable();

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

    @FXML
    void anuller(ActionEvent event) {
        // Récupérer la Stage (fenêtre) actuelle via le bouton Anuller
        if (Anuller != null && Anuller.getScene() != null) {
            Stage stage = (Stage) Anuller.getScene().getWindow();
            stage.close();
        }
    }
}