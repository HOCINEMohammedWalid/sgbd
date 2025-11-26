package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.These; // Assurez-vous d'importer la classe These
import com.sgeb.sgbd.model.enums.Categorie; // Assurez-vous d'importer la classe Categorie
import com.sgeb.sgbd.model.exception.DocumentInexistantException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DetailsTheseController implements DetailsControllerBase {

    // ==== FXML Fields ====
    @FXML
    private Button Anuller;
    @FXML
    private TextField Auteurs;
    @FXML
    private Button Update;
    @FXML
    private Spinner<Integer> annee_p; // Année de soutenance
    @FXML
    private TextField auteur_p; // Auteur principal (redondant avec Auteurs, mais conservé)
    @FXML
    private ChoiceBox<Categorie> catg; // Catégorie (domaine)
    @FXML
    private TextField date_s; // Date de soutenance (String, format YYYY-MM-DD ou DatePicker si nécessaire)
    @FXML
    private TextField discp; // Discipline
    @FXML
    private TextField dr; // Directeur de recherche
    @FXML
    private TextField editeur;
    @FXML
    private ChoiceBox<String> langue;
    @FXML
    private TextField mot_cles;
    @FXML
    private TextArea resume;
    @FXML
    private TextField titre;
    @FXML
    private TextField tp_acees; // Type d'accès
    @FXML
    private TextField univ; // Université

    // ==== Managers et Parent ====
    private Document document;
    private DocumentManager documentManager;
    private DocumentsController parent;

    // =============================================
    // INITIALIZATION (FXML)
    // =============================================
    @FXML
    public void initialize() {
        // --- Configuration des Spinners ---
        if (annee_p != null) {
            annee_p.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1800, 2100, 2025));
        }

        // --- Configuration des ChoiceBoxes ---
        if (langue != null) {
            langue.getItems().addAll("FR", "Anglais", "Arabe", "Autre");
        }
        if (catg != null) {
            catg.getItems().addAll(Categorie.values());
        }
    }

    // =============================================
    // INTERFACE DetailsControllerBase
    // =============================================

    @Override
    public void setDocument(Document document) {
        if (document instanceof These) {
            this.document = document;
            initializeFields((These) document);
        } else {
            System.err.println("Erreur: Le document n'est pas une Thèse.");
        }
    }

    @Override
    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    @Override
    public void setParent(DocumentsController parent) {
        this.parent = parent;
    }

    // =============================================
    // CHAMP INITIALIZATION
    // =============================================
    private void initializeFields(These these) {

        // Champs communs/Document
        if (titre != null)
            titre.setText(these.getTitre());
        if (editeur != null)
            editeur.setText(these.getEditeur());
        if (resume != null)
            resume.setText(these.getResume());
        if (Auteurs != null)
            Auteurs.setText(String.join(", ", these.getAuteurs()));
        if (mot_cles != null)
            mot_cles.setText(String.join(", ", these.getMotsCles()));

        // Initialisation des valeurs (ChoiceBox/Spinner)
        if (annee_p != null && annee_p.getValueFactory() != null) {
            annee_p.getValueFactory().setValue(these.getAnneePublication());
        }
        if (langue != null && langue.getSelectionModel() != null) {
            langue.getSelectionModel().select(these.getLangue());
        }
        if (catg != null && catg.getSelectionModel() != null) {
            catg.getSelectionModel().select(these.getCategorie());
        }

        // Champs spécifiques à Thèse (Utilisation de getters supposés)
        if (dr != null)
            dr.setText(these.getDirecteurRecherche());
        if (univ != null)
            univ.setText(these.getUniversite());
        if (discp != null)
            discp.setText(these.getDiscipline());
        if (date_s != null)
            date_s.setText(these.getDateSoutenance().toString()); // Si c'est un String
        if (tp_acees != null)
            tp_acees.setText(these.getTypeAcces());
        if (auteur_p != null)
            auteur_p.setText(these.getAuteurPrincipal()); // Si ce champ existe
    }

    // =============================================
    // LOGIQUE DE MISE À JOUR (UPDATE)
    // =============================================
    @FXML
    void update(ActionEvent event) {
        if (documentManager == null || !(document instanceof These)) {
            showAlert("Erreur", "Problème d'initialisation du manager ou du document.", Alert.AlertType.ERROR);
            return;
        }

        These these = (These) this.document;

        // --- 1. Préparation des valeurs ---
        final Categorie nouvelleCategorie = catg.getValue();
        final Integer nouvelleAnnee = annee_p != null ? annee_p.getValue() : null;

        // Validation simple
        if (nouvelleCategorie == null || nouvelleAnnee == null) {
            showAlert("Erreur de donnée", "Veuillez remplir les champs Année et Catégorie.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // 2. Appeler le manager pour appliquer toutes les modifications
            boolean success = documentManager.modifierDocument(these.getIdDocument(), doc -> {

                if (doc instanceof These) {
                    These theseModifie = (These) doc;

                    // 1. Mise à jour des champs communs
                    if (titre != null)
                        theseModifie.setTitre(titre.getText());
                    if (editeur != null)
                        theseModifie.setEditeur(editeur.getText());
                    if (resume != null)
                        theseModifie.setResume(resume.getText());
                    theseModifie.setAnneePublication(nouvelleAnnee);
                    theseModifie.setCategorie(nouvelleCategorie);
                    if (langue != null)
                        theseModifie.setLangue(langue.getValue());

                    // Listes (Auteurs et Mots-clés)
                    if (Auteurs != null) {
                        List<String> nouveauxAuteurs = Arrays.asList(Auteurs.getText().split("\\s*,\\s*"));
                        theseModifie.setAuteurs(nouveauxAuteurs);
                    }
                    if (mot_cles != null) {
                        List<String> nouveauxMotsCles = Arrays.asList(mot_cles.getText().split("\\s*,\\s*"));
                        theseModifie.setMotsCles(nouveauxMotsCles);
                    }

                    // 2. Mise à jour des champs spécifiques à Thèse (Utilisation de setters
                    // supposés)
                    if (dr != null)
                        theseModifie.setDirecteurRecherche(dr.getText());
                    if (univ != null)
                        theseModifie.setUniversite(univ.getText());
                    if (discp != null)
                        theseModifie.setDiscipline(discp.getText());
                    if (date_s != null)
                        theseModifie.setDateSoutenance(date_s.getText());
                    if (tp_acees != null)
                        theseModifie.setTypeAcces(tp_acees.getText());
                    if (auteur_p != null)
                        theseModifie.setAuteurPrincipal(auteur_p.getText());
                }
            });

            if (success) {
                if (parent != null) {
                    parent.refreshTable();
                }
                showAlert("Succès", "Thèse mise à jour avec succès.", Alert.AlertType.INFORMATION);
                anuller(null); // Ferme la fenêtre
            } else {
                showAlert("Erreur", "Échec de la mise à jour : Document non trouvé ou modification annulée.",
                        Alert.AlertType.ERROR);
            }

        } catch (DocumentInexistantException e) {
            showAlert("Erreur", "Le document à modifier n'existe plus.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur BDD", "Une erreur est survenue lors de la sauvegarde : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // =============================================
    // LOGIQUE D'ANNULATION (ANNULER)
    // =============================================
    @FXML
    void anuller(ActionEvent event) {
        // Tentative de récupérer la Stage à partir du bouton Anuller
        if (Anuller != null && Anuller.getScene() != null && Anuller.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) Anuller.getScene().getWindow();
            stage.close();
        }
        // Option de secours via le bouton Update
        else if (Update != null && Update.getScene() != null && Update.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) Update.getScene().getWindow();
            stage.close();
        }
    }

    // =============================================
    // UTILS
    // =============================================
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}