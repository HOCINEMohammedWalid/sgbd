package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.DVD;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.enums.Categorie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Arrays;
import java.util.List;

public class AjouterDVDController {

    private DocumentManager documentManager;
    private DocumentsController parentController;

    @FXML
    private TextField Auteurs;

    @FXML
    private Spinner<Integer> annee_p;

    @FXML
    private ChoiceBox<Categorie> catg;

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
    private TextField realisateur;

    @FXML
    private Spinner<Integer> duree;

    @FXML
    private ChoiceBox<String> cal;

    @FXML
    public void initialize() {
        annee_p.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2100, 2024));
        duree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 90)); // durée en minutes

        langue.getItems().addAll("Français", "Anglais", "Arabe");
        catg.getItems().addAll(Categorie.values());
        cal.getItems().addAll("G", "PG", "PG-13", "R", "NC-17");
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
                showError("Le titre du DVD ne peut pas être vide.");
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

            if (annee_p.getValue() < 1900 || annee_p.getValue() > 2100) {
                showError("L'année doit être comprise entre 1900 et 2100.");
                return;
            }

            if (realisateur.getText() == null || realisateur.getText().trim().isEmpty()) {
                showError("Veuillez entrer le nom du réalisateur.");
                return;
            }

            if (duree.getValue() <= 0) {
                showError("La durée doit être un nombre positif.");
                return;
            }

            if (cal.getValue() == null || cal.getValue().trim().isEmpty()) {
                showError("Veuillez sélectionner une classification.");
                return;
            }

            if (mot_cles.getText() == null || mot_cles.getText().trim().isEmpty()) {
                showError("Veuillez entrer au moins un mot-clé.");
                return;
            }

            // --- Création du DVD ---
            List<String> auteursList = Arrays.asList(Auteurs.getText().split(","));
            List<String> motsClesList = Arrays.asList(mot_cles.getText().split(","));

            DVD dvd = new DVD(
                    0, // id auto
                    titre.getText(),
                    auteursList,
                    annee_p.getValue(),
                    editeur.getText(),
                    resume.getText(),
                    catg.getValue(),
                    motsClesList,
                    langue.getValue(),
                    realisateur.getText(),
                    duree.getValue(),
                    cal.getValue());

            documentManager.ajouterDocument(dvd);

            showInfo("DVD ajouté avec succès !");
            parentController.refreshTable();
            titre.getScene().getWindow().hide();

        } catch (Exception e) {
            showError("Impossible d'ajouter le DVD : " + e.getMessage());
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
