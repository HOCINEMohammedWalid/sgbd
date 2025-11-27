package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager; // Nécessaire si DetailsControllerBase l'exige
import com.sgeb.sgbd.model.EmpruntManager;
import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.ArticleUniversitaire;
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

public class DetailsArticleUnivControllerNonAdmin implements Initializable, DetailsControllerBase {

    // Manager pour l'action d'emprunt et le Document
    private EmpruntManager empruntManager;
    private ArticleUniversitaire article;
    private Adherent adherent;

    // Assurez-vous que cette méthode est déclarée dans DetailsControllerBase si
    // elle est utilisée
    // dans la classe DocumentsController pour passer l'utilisateur courant.
    // Si DetailsControllerBase n'est pas modifiable, vous devez la définir
    // directement ici.
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
    private TextField doi;
    @FXML
    private TextField editeur;
    @FXML
    private TextField langue;
    @FXML
    private TextField mot_cles;
    @FXML
    private TextField nb_pages;
    @FXML
    private TextField page_end;
    @FXML
    private TextField page_start;
    @FXML
    private TextArea resume;
    @FXML
    private TextField titre;
    @FXML
    private TextField titre_revue;
    @FXML
    private TextField volume;

    // =============================================
    // INJECTION DU DOCUMENT ET MANAGER
    // =============================================

    public void setEmpruntManager(EmpruntManager empruntManager) {
        this.empruntManager = empruntManager;
    }

    @Override
    public void setDocument(Document document) {
        if (document instanceof ArticleUniversitaire) {
            this.article = (ArticleUniversitaire) document;
            initializeFields();
        } else {
            System.err.println("Erreur: Le document n'est pas un Article Universitaire.");
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
        if (doi != null)
            doi.setEditable(!readOnly);
        if (editeur != null)
            editeur.setEditable(!readOnly);
        if (langue != null)
            langue.setEditable(!readOnly);
        if (mot_cles != null)
            mot_cles.setEditable(!readOnly);
        if (nb_pages != null)
            nb_pages.setEditable(!readOnly);
        if (page_end != null)
            page_end.setEditable(!readOnly);
        if (page_start != null)
            page_start.setEditable(!readOnly);
        if (resume != null)
            resume.setEditable(!readOnly);
        if (titre != null)
            titre.setEditable(!readOnly);
        if (titre_revue != null)
            titre_revue.setEditable(!readOnly);
        if (volume != null)
            volume.setEditable(!readOnly);
    }

    private void initializeFields() {
        if (this.article != null) {
            // Champs communs/Document
            if (titre != null)
                titre.setText(article.getTitre());
            if (editeur != null)
                editeur.setText(article.getEditeur());
            if (resume != null)
                resume.setText(article.getResume());

            // Listes converties en String
            String auteursStr = String.join(", ", article.getAuteurs());
            if (Auteurs != null)
                Auteurs.setText(auteursStr);
            String motsClesStr = String.join(", ", article.getMotsCles());
            if (mot_cles != null)
                mot_cles.setText(motsClesStr);

            // Enums et Entiers
            if (annee_p != null)
                annee_p.setText(String.valueOf(article.getAnneePublication()));
            if (langue != null)
                langue.setText(article.getLangue());
            if (catg != null)
                catg.setText(article.getCategorie().toString());

            // Champs spécifiques à ArticleUniversitaire
            if (titre_revue != null)
                titre_revue.setText(article.getTitreRevue());
            if (doi != null)
                doi.setText(article.getDOI());

            // Gestion des pages
            String pages = article.getPages(); // Ex: "50-78"
            if (pages != null && pages.contains("-")) {
                String[] pagesParts = pages.split("-");
                if (pagesParts.length == 2) {
                    if (page_start != null)
                        page_start.setText(pagesParts[0].trim());
                    if (page_end != null)
                        page_end.setText(pagesParts[1].trim());
                }
            } else if (page_start != null && pages != null) {
                // Affichage direct si le format n'est pas "start-end"
                page_start.setText(pages);
            }
            // Laisser page_end vide dans ce cas

            if (volume != null)
                volume.setText(String.valueOf(article.getVolume()));

            // nb_pages est souvent le numéro de la revue pour un article
            if (nb_pages != null)
                nb_pages.setText(String.valueOf(article.getNumero()));
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

    DocumentsControllerNonAdmin documentsControllerNonAdmin;

    public void setParentN(DocumentsControllerNonAdmin d) {
        documentsControllerNonAdmin = d;
    }

    @FXML
    void emprunter(ActionEvent event) {
        // Validation que les objets nécessaires sont présents
        if (empruntManager == null || article == null || adherent == null) {
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
            empruntManager.emprunter(article, adherent);

            // Succès : Afficher le message de réussite
            showAlert("Emprunt réussi",
                    "Confirmation",
                    "L'article \"" + article.getTitre() + "\" a été emprunté avec succès par " + adherent.getNom()
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
}