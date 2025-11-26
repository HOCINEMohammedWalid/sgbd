package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.Livre; // Assurez-vous d'importer la classe Livre
import com.sgeb.sgbd.model.enums.Categorie; // Assurez-vous d'importer la classe Categorie
import com.sgeb.sgbd.model.exception.DocumentInexistantException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DetailsLivreController implements DetailsControllerBase {

    // ==== FXML Fields ====
    @FXML
    private Button Anuller;
    @FXML
    private TextField Auteurs;
    @FXML
    private Button Update;
    @FXML
    private Spinner<Integer> annee_p; // Année de publication
    @FXML
    private ChoiceBox<Categorie> catg; // Catégorie
    @FXML
    private TextField collec; // Spécifique Livre: Collection
    @FXML
    private TextField editeur;
    @FXML
    private TextField isbn; // Spécifique Livre: ISBN
    @FXML
    private ChoiceBox<String> langue;
    @FXML
    private TextField mot_cles;
    @FXML
    private Spinner<Integer> nbpages; // Spécifique Livre: Nombre de pages
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
        if (nbpages != null) {
            nbpages.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 100));
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
        if (document instanceof Livre) {
            this.document = document;
            initializeFields((Livre) document);
        } else {
            System.err.println("Erreur: Le document n'est pas un Livre.");
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
    private void initializeFields(Livre livre) {

        // Champs communs/Document
        if (titre != null)
            titre.setText(livre.getTitre());
        if (editeur != null)
            editeur.setText(livre.getEditeur());
        if (resume != null)
            resume.setText(livre.getResume());
        if (Auteurs != null)
            Auteurs.setText(String.join(", ", livre.getAuteurs()));
        if (mot_cles != null)
            mot_cles.setText(String.join(", ", livre.getMotsCles()));

        // Initialisation des valeurs (ChoiceBox/Spinner)
        if (annee_p != null && annee_p.getValueFactory() != null) {
            annee_p.getValueFactory().setValue(livre.getAnneePublication());
        }
        if (langue != null && langue.getSelectionModel() != null) {
            langue.getSelectionModel().select(livre.getLangue());
        }
        if (catg != null && catg.getSelectionModel() != null) {
            catg.getSelectionModel().select(livre.getCategorie());
        }

        // Champs spécifiques à Livre
        if (isbn != null)
            isbn.setText(livre.getISBN());
        if (collec != null)
            collec.setText(livre.getCollection());

        if (nbpages != null && nbpages.getValueFactory() != null) {
            nbpages.getValueFactory().setValue(livre.getNbPages());
        }
    }

    // =============================================
    // LOGIQUE DE MISE À JOUR (UPDATE)
    // =============================================
    @FXML
    void update(ActionEvent event) {
        if (documentManager == null || !(document instanceof Livre)) {
            showAlert("Erreur", "Problème d'initialisation du manager ou du document.", Alert.AlertType.ERROR);
            return;
        }

        Livre livre = (Livre) this.document;

        // --- 1. Préparation des valeurs ---
        final Categorie nouvelleCategorie = catg.getValue();
        final Integer nouvelleAnnee = annee_p != null ? annee_p.getValue() : null;
        final Integer nouveauNbPages = nbpages != null ? nbpages.getValue() : null;

        // Validation simple
        if (nouvelleCategorie == null || nouvelleAnnee == null || nouveauNbPages == null) {
            showAlert("Erreur de donnée", "Veuillez remplir les champs Année, Catégorie et Nombre de pages.",
                    Alert.AlertType.ERROR);
            return;
        }

        try {
            // 2. Appeler le manager pour appliquer toutes les modifications
            boolean success = documentManager.modifierDocument(livre.getIdDocument(), doc -> {

                if (doc instanceof Livre) {
                    Livre livreModifie = (Livre) doc;

                    // 1. Mise à jour des champs communs
                    if (titre != null)
                        livreModifie.setTitre(titre.getText());
                    if (editeur != null)
                        livreModifie.setEditeur(editeur.getText());
                    if (resume != null)
                        livreModifie.setResume(resume.getText());

                    // Spinners
                    livreModifie.setAnneePublication(nouvelleAnnee);

                    // ChoiceBoxes/Enums
                    livreModifie.setCategorie(nouvelleCategorie);
                    if (langue != null)
                        livreModifie.setLangue(langue.getValue());

                    // Listes (Auteurs et Mots-clés)
                    if (Auteurs != null) {
                        List<String> nouveauxAuteurs = Arrays.asList(Auteurs.getText().split("\\s*,\\s*"));
                        livreModifie.setAuteurs(nouveauxAuteurs);
                    }
                    if (mot_cles != null) {
                        List<String> nouveauxMotsCles = Arrays.asList(mot_cles.getText().split("\\s*,\\s*"));
                        livreModifie.setMotsCles(nouveauxMotsCles);
                    }

                    // 2. Mise à jour des champs spécifiques au Livre
                    if (isbn != null)
                        livreModifie.setISBN(isbn.getText());
                    if (collec != null)
                        livreModifie.setCollection(collec.getText());
                    livreModifie.setNbPages(nouveauNbPages);
                }
            });

            if (success) {
                if (parent != null) {
                    parent.refreshTable();
                }
                showAlert("Succès", "Livre mis à jour avec succès.", Alert.AlertType.INFORMATION);
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