package com.sgeb.sgbd.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EBook;
import com.sgeb.sgbd.model.enums.Categorie;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AjouterEbookController {

    private DocumentManager documentManager;
    private DocumentsController parentController;

    @FXML
    private TextField titre;
    @FXML
    private TextField Auteurs;
    @FXML
    private TextField editeur;
    @FXML
    private Spinner<Integer> annee_p;
    @FXML
    private ChoiceBox<Categorie> catg;
    @FXML
    private TextField mot_cles;
    @FXML
    private ChoiceBox<String> langue;
    @FXML
    private TextArea resume;
    @FXML
    private TextField url;

    @FXML
    private CheckBox drm;
    @FXML
    private ChoiceBox<String> format;

    @FXML
    public void initialize() {
        // Spinner pour l'année
        annee_p.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1800, 2100, 2024));

        // Choix de catégorie
        catg.getItems().addAll(Categorie.values());

        // Choix de langue
        langue.getItems().addAll("Français", "Anglais", "Arabe");

        // Choix de format
        format.getItems().addAll("PDF", "EPUB", "MOBI");
        format.setValue("PDF");
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

            if (url.getText() == null || url.getText().trim().isEmpty()) {
                showError("Veuillez entrer l'URL de l'EBook.");
                return;
            }

            List<String> auteursList = Arrays.asList(Auteurs.getText().split(","));
            List<String> motsClesList = mot_cles.getText() != null && !mot_cles.getText().trim().isEmpty()
                    ? Arrays.asList(mot_cles.getText().split(","))
                    : new ArrayList<>();

            EBook ebook = new EBook(
                    0,
                    titre.getText(),
                    auteursList,
                    annee_p.getValue(),
                    editeur.getText(),
                    resume.getText(),
                    catg.getValue(),
                    motsClesList,
                    langue.getValue(),
                    url.getText(),
                    format.getValue(),
                    drm.isSelected());

            documentManager.ajouterDocument(ebook);
            showInfo("EBook ajouté !");
            parentController.refreshTable();
            titre.getScene().getWindow().hide();

        } catch (Exception e) {
            showError("Impossible d'ajouter l'EBook : " + e.getMessage());
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
