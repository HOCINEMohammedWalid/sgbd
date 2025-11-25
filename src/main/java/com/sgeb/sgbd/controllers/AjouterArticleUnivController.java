package com.sgeb.sgbd.controllers;

import java.util.Arrays;
import java.util.List;

import com.sgeb.sgbd.model.ArticleUniversitaire;
import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.DocumentManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AjouterArticleUnivController {

    private DocumentManager documentManager;
    private DocumentsController parentController;

    @FXML
    private TextField Auteurs;

    @FXML
    private Spinner<Integer> annee_p;

    @FXML
    private ChoiceBox<Categorie> catg;

    @FXML
    private TextField doi;

    @FXML
    private TextField editeur;

    @FXML
    private ChoiceBox<String> langue;

    @FXML
    private TextField mot_cles;

    @FXML
    private Spinner<Integer> nb_pages;

    @FXML
    private Spinner<Integer> pages_end;

    @FXML
    private Spinner<Integer> pages_start;

    @FXML
    private TextArea resume;

    @FXML
    private TextField titre;

    @FXML
    private TextField titre_revue;

    @FXML
    private Spinner<Integer> volume;

    @FXML
    public void initialize() {
        annee_p.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1800, 2100, 2024));
        pages_start.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999));
        pages_end.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999));
        volume.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50));
        nb_pages.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999));
        langue.getItems().addAll("Français", "Anglais", "Arabe");
        catg.getItems().addAll(Categorie.values());
    }

    public void setManager(DocumentManager manager) {
        this.documentManager = manager;
    }

    public void setParent(DocumentsController parent) {
        this.parentController = parent;
    }

    @FXML
    void ajouter(ActionEvent event) {
        try {
            // --- Validation des champs ---
            if (titre.getText() == null || titre.getText().trim().isEmpty()) {
                showError("Le titre ne peut pas être vide.");
                return;
            }

            if (Auteurs.getText() == null || Auteurs.getText().trim().isEmpty()) {
                showError("Veuillez entrer au moins un auteur.");
                return;
            }

            if (catg.getValue() == null) {
                showError("Veuillez sélectionner une catégorie.");
                return;
            }

            if (langue.getValue() == null || langue.getValue().trim().isEmpty()) {
                showError("Veuillez sélectionner une langue.");
                return;
            }

            if (annee_p.getValue() < 1800 || annee_p.getValue() > 2100) {
                showError("L'année doit être comprise entre 1800 et 2100.");
                return;
            }

            if (pages_start.getValue() > pages_end.getValue()) {
                showError("La page de début ne peut pas être supérieure à la page de fin.");
                return;
            }

            if (mot_cles.getText() == null || mot_cles.getText().trim().isEmpty()) {
                showError("Veuillez entrer au moins un mot-clé.");
                return;
            }

            // --- Création de l'article ---
            List<String> auteursList = Arrays.asList(Auteurs.getText().split(","));
            List<String> motsClesList = Arrays.asList(mot_cles.getText().split(","));
            String pages = pages_start.getValue() + "-" + pages_end.getValue();

            ArticleUniversitaire art = new ArticleUniversitaire(
                    0, // id auto dans DB
                    titre.getText(),
                    auteursList,
                    annee_p.getValue(),
                    editeur.getText(),
                    resume.getText(),
                    catg.getValue(),
                    motsClesList,
                    langue.getValue(),
                    titre_revue.getText(),
                    volume.getValue(),
                    nb_pages.getValue(),
                    pages,
                    doi.getText());

            documentManager.ajouterDocument(art);

            showInfo("Article universitaire ajouté !");

            parentController.refreshTable();
            titre.getScene().getWindow().hide();

        } catch (Exception e) {
            showError("Impossible d'ajouter l'article : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert al = new Alert(Alert.AlertType.ERROR);
        al.setHeaderText("Erreur");
        al.setContentText(message);
        al.showAndWait();
    }

    private void showInfo(String message) {
        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.setHeaderText("Succès");
        al.setContentText(message);
        al.showAndWait();
    }
}
