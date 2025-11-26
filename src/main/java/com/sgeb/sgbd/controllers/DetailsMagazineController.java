package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.Magazine; // Assurez-vous d'importer la classe Magazine
import com.sgeb.sgbd.model.enums.Categorie; // Assurez-vous d'importer la classe Categorie
import com.sgeb.sgbd.model.exception.DocumentInexistantException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DetailsMagazineController implements DetailsControllerBase {

    // ==== FXML Fields ====
    @FXML
    private Button Anuller;
    @FXML
    private TextField Auteurs;
    @FXML
    private Button Update;
    @FXML
    private Spinner<Integer> annee_p; // Année de publication (du magazine ou du numéro)
    @FXML
    private ChoiceBox<Categorie> catg; // Catégorie
    @FXML
    private TextField date_de_pub; // Spécifique Magazine: Date exacte de publication
    @FXML
    private TextField editeur;
    @FXML
    private ChoiceBox<String> langue;
    @FXML
    private TextField mot_cles;
    @FXML
    private Spinner<Integer> num; // Spécifique Magazine: Numéro du magazine
    @FXML
    private TextField periodicite; // Spécifique Magazine: Périodicité
    @FXML
    private TextArea resume;
    @FXML
    private TextField titre;

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
        // Numéro du magazine
        if (num != null) {
            num.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
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
        if (document instanceof Magazine) {
            this.document = document;
            initializeFields((Magazine) document);
        } else {
            System.err.println("Erreur: Le document n'est pas un Magazine.");
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
    private void initializeFields(Magazine magazine) {

        // Champs communs/Document
        if (titre != null)
            titre.setText(magazine.getTitre());
        if (editeur != null)
            editeur.setText(magazine.getEditeur());
        if (resume != null)
            resume.setText(magazine.getResume());
        if (Auteurs != null)
            Auteurs.setText(String.join(", ", magazine.getAuteurs()));
        if (mot_cles != null)
            mot_cles.setText(String.join(", ", magazine.getMotsCles()));

        // Initialisation des valeurs (ChoiceBox/Spinner)
        if (annee_p != null && annee_p.getValueFactory() != null) {
            annee_p.getValueFactory().setValue(magazine.getAnneePublication());
        }
        if (langue != null && langue.getSelectionModel() != null) {
            langue.getSelectionModel().select(magazine.getLangue());
        }
        if (catg != null && catg.getSelectionModel() != null) {
            catg.getSelectionModel().select(magazine.getCategorie());
        }

        // Champs spécifiques à Magazine
        if (num != null && num.getValueFactory() != null) {
            num.getValueFactory().setValue(magazine.getNumero());
        }
        if (periodicite != null)
            periodicite.setText(magazine.getPeriodicite());
        if (date_de_pub != null)
            date_de_pub.setText(magazine.getDatePublication().toString()); // Si c'est une String
    }

    // =============================================
    // LOGIQUE DE MISE À JOUR (UPDATE)
    // =============================================
    @FXML
    void update(ActionEvent event) {
        if (documentManager == null || !(document instanceof Magazine)) {
            showAlert("Erreur", "Problème d'initialisation du manager ou du document.", Alert.AlertType.ERROR);
            return;
        }

        Magazine magazine = (Magazine) this.document;

        // --- 1. Préparation des valeurs ---
        final Categorie nouvelleCategorie = catg.getValue();
        final Integer nouvelleAnnee = annee_p != null ? annee_p.getValue() : null;
        final Integer nouveauNum = num != null ? num.getValue() : null;

        // Validation simple
        if (nouvelleCategorie == null || nouvelleAnnee == null || nouveauNum == null) {
            showAlert("Erreur de donnée", "Veuillez remplir les champs Année, Catégorie et Numéro.",
                    Alert.AlertType.ERROR);
            return;
        }

        try {
            // 2. Appeler le manager pour appliquer toutes les modifications
            boolean success = documentManager.modifierDocument(magazine.getIdDocument(), doc -> {

                if (doc instanceof Magazine) {
                    Magazine magazineModifie = (Magazine) doc;

                    // 1. Mise à jour des champs communs
                    if (titre != null)
                        magazineModifie.setTitre(titre.getText());
                    if (editeur != null)
                        magazineModifie.setEditeur(editeur.getText());
                    if (resume != null)
                        magazineModifie.setResume(resume.getText());

                    // Spinners
                    magazineModifie.setAnneePublication(nouvelleAnnee);

                    // ChoiceBoxes/Enums
                    magazineModifie.setCategorie(nouvelleCategorie);
                    if (langue != null)
                        magazineModifie.setLangue(langue.getValue());

                    // Listes (Auteurs et Mots-clés)
                    if (Auteurs != null) {
                        List<String> nouveauxAuteurs = Arrays.asList(Auteurs.getText().split("\\s*,\\s*"));
                        magazineModifie.setAuteurs(nouveauxAuteurs);
                    }
                    if (mot_cles != null) {
                        List<String> nouveauxMotsCles = Arrays.asList(mot_cles.getText().split("\\s*,\\s*"));
                        magazineModifie.setMotsCles(nouveauxMotsCles);
                    }

                    // 2. Mise à jour des champs spécifiques au Magazine
                    magazineModifie.setNumero(nouveauNum);
                    if (periodicite != null)
                        magazineModifie.setPeriodicite(periodicite.getText());
                    if (date_de_pub != null)
                        magazineModifie.setDatePublication(date_de_pub.getText());
                }
            });

            if (success) {
                if (parent != null) {
                    parent.refreshTable();
                }
                showAlert("Succès", "Magazine mis à jour avec succès.", Alert.AlertType.INFORMATION);
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