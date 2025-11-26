package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.DVD; // Assurez-vous d'importer la classe DVD
import com.sgeb.sgbd.model.enums.Categorie; // Assurez-vous d'importer la classe Categorie
import com.sgeb.sgbd.model.exception.DocumentInexistantException;
// Import DocumentsController si setParent est utilisé
// import com.sgeb.sgbd.controllers.DocumentsController; 

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// Implémente DetailsControllerBase pour la compatibilité avec DocumentsController
public class DetailsDVDController implements DetailsControllerBase {

    // ==== FXML Fields (Basés sur votre squelette) ====
    @FXML
    private Button Anuller; // Ajouté pour la cohérence
    @FXML
    private Button update; // Ajouté pour la cohérence

    @FXML
    private TextField Auteurs; // Correspond au réalisateur/auteurs (si applicable)

    @FXML
    private Spinner<Integer> annee_p; // Année de publication

    @FXML
    private ChoiceBox<Categorie> catg; // Catégorie (Genre)

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

    // Champs spécifiques au DVD
    @FXML
    private TextField realisateur; // Remplacer le champ TitreRevue par Réalisateur

    @FXML
    private Spinner<Integer> duree; // Durée du film (en minutes)

    @FXML
    private TextField classification; // Classification (ex: R, PG-13, Tout public)

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
        // Année de publication
        if (annee_p != null) {
            annee_p.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1800, 2100, 2025));
        }
        // Durée du DVD (en minutes)
        if (duree != null) {
            duree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 90));
        }

        // --- Configuration des ChoiceBoxes ---
        if (langue != null) {
            langue.getItems().addAll("FR", "Anglais", "Arabe", "Autre");
        }
        if (catg != null) {
            // Remplissage de la ChoiceBox avec les valeurs de l'énum Categorie
            catg.getItems().addAll(Categorie.values());
        }
    }

    // =============================================
    // INTERFACE DetailsControllerBase
    // =============================================

    @Override
    public void setDocument(Document document) {
        if (document instanceof DVD) {
            System.out.println(document);
            this.document = document;
            initializeFields((DVD) document);
        } else {
            System.err.println("Erreur: Le document n'est pas un DVD.");
        }
    }

    @Override
    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    // Assurez-vous que DocumentsController est importé et accessible
    @Override
    public void setParent(DocumentsController parent) {
        this.parent = parent;
    }

    // =============================================
    // CHAMP INITIALIZATION
    // =============================================
    private void initializeFields(DVD dvd) {
        System.out.println(dvd.getRealisateur());
        // Champs communs (Basés sur la classe Document)
        if (titre != null)
            titre.setText(dvd.getTitre());
        if (editeur != null)
            editeur.setText(dvd.getEditeur());
        if (resume != null)
            resume.setText(dvd.getResume());

        // Auteurs est souvent utilisé pour Réalisateur dans un contexte DVD
        // Si la classe Document gère Auteurs, on l'affiche, sinon on affiche
        // Réalisateur dans un champ spécifique.
        if (Auteurs != null)
            Auteurs.setText(String.join(", ", dvd.getAuteurs()));
        if (mot_cles != null)
            mot_cles.setText(String.join(", ", dvd.getMotsCles()));

        // Initialisation des valeurs (ChoiceBox/Spinner)
        if (annee_p != null && annee_p.getValueFactory() != null) {
            annee_p.getValueFactory().setValue(dvd.getAnneePublication());
        }
        if (langue != null && langue.getSelectionModel() != null) {
            langue.getSelectionModel().select(dvd.getLangue());
        }
        if (catg != null && catg.getSelectionModel() != null) {
            catg.getSelectionModel().select(dvd.getCategorie());
        }

        // Champs spécifiques à DVD
        if (realisateur != null)
            realisateur.setText(dvd.getRealisateur());
        if (classification != null)
            classification.setText(dvd.getClassification());

        if (duree != null && duree.getValueFactory() != null) {
            duree.getValueFactory().setValue(dvd.getDuree());
        }
    }

    // =============================================
    // LOGIQUE DE MISE À JOUR (UPDATE)
    // =============================================
    @FXML
    void update(ActionEvent event) {
        if (documentManager == null || !(document instanceof DVD)) {
            showAlert("Erreur", "Problème d'initialisation du manager ou du document.", Alert.AlertType.ERROR);
            return;
        }

        DVD dvd = (DVD) this.document;

        // --- 1. Préparation des valeurs ---
        final Categorie nouvelleCategorie = catg.getValue();
        final Integer nouvelleAnnee = annee_p != null ? annee_p.getValue() : null;
        final Integer nouvelleDuree = duree != null ? duree.getValue() : null;

        // Validation simple
        if (nouvelleCategorie == null || nouvelleAnnee == null || nouvelleDuree == null) {
            showAlert("Erreur de donnée", "Veuillez remplir tous les champs obligatoires (Année, Catégorie, Durée).",
                    Alert.AlertType.ERROR);
            return;
        }

        try {
            // 2. Appeler le manager pour appliquer toutes les modifications via le Consumer
            boolean success = documentManager.modifierDocument(dvd.getIdDocument(), doc -> {

                if (doc instanceof DVD) {
                    DVD dvdModifie = (DVD) doc;

                    // 1. Mise à jour des champs communs
                    if (titre != null)
                        dvdModifie.setTitre(titre.getText());
                    if (editeur != null)
                        dvdModifie.setEditeur(editeur.getText());
                    if (resume != null)
                        dvdModifie.setResume(resume.getText());

                    // Spinners
                    dvdModifie.setAnneePublication(nouvelleAnnee);

                    // ChoiceBoxes/Enums
                    dvdModifie.setCategorie(nouvelleCategorie);
                    if (langue != null)
                        dvdModifie.setLangue(langue.getValue());

                    // Listes (Auteurs et Mots-clés)
                    if (Auteurs != null) {
                        List<String> nouveauxAuteurs = Arrays.asList(Auteurs.getText().split("\\s*,\\s*"));
                        dvdModifie.setAuteurs(nouveauxAuteurs);
                    }
                    if (mot_cles != null) {
                        List<String> nouveauxMotsCles = Arrays.asList(mot_cles.getText().split("\\s*,\\s*"));
                        dvdModifie.setMotsCles(nouveauxMotsCles);
                    }

                    // 2. Mise à jour des champs spécifiques au DVD
                    if (realisateur != null)
                        dvdModifie.setRealisateur(realisateur.getText());
                    if (classification != null)
                        dvdModifie.setClassification(classification.getText());

                    // Durée (Spinner)
                    dvdModifie.setDuree(nouvelleDuree);
                }
            });

            if (success) {
                // Notifie le parent pour rafraîchir la table des documents
                if (parent != null) {
                    parent.refreshTable();
                }
                showAlert("Succès", "DVD mis à jour avec succès.", Alert.AlertType.INFORMATION);
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
        // Fermer la fenêtre
        if (Anuller != null && Anuller.getScene() != null && Anuller.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) Anuller.getScene().getWindow();
            stage.close();
        } else {
            // Si la méthode est appelée par update(null), fermer via le bouton update
            if (update != null && update.getScene() != null && update.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) update.getScene().getWindow();
                stage.close();
            }
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