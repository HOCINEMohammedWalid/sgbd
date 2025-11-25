package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Magazine;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.enums.Categorie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AjouterMagazineController {

    private DocumentManager documentManager;
    private DocumentsController parentController;

    @FXML
    private TextField Auteurs;

    @FXML
    private Spinner<Integer> annee_p;

    @FXML
    private ChoiceBox<Categorie> catg;

    @FXML
    private TextField date_de_pub;

    @FXML
    private TextField editeur;

    @FXML
    private ChoiceBox<String> langue;

    @FXML
    private TextField mot_cles;

    @FXML
    private Spinner<Integer> num;

    @FXML
    private TextField periodicite;

    @FXML
    private TextArea resume;

    @FXML
    private TextField titre;

    @FXML
    public void initialize() {
        annee_p.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2100, 2024));
        num.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));

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
            // --- Validation ---
            if (titre.getText().trim().isEmpty()) {
                showError("Le titre ne peut pas être vide.");
                return;
            }

            if (Auteurs.getText().trim().isEmpty()) {
                showError("Veuillez entrer au moins un auteur.");
                return;
            }

            if (catg.getValue() == null) {
                showError("Veuillez sélectionner une catégorie.");
                return;
            }

            if (langue.getValue() == null) {
                showError("Veuillez sélectionner une langue.");
                return;
            }

            if (editeur.getText().trim().isEmpty()) {
                showError("Veuillez saisir un éditeur.");
                return;
            }

            if (resume.getText().trim().isEmpty()) {
                showError("Veuillez saisir un résumé.");
                return;
            }

            if (annee_p.getValue() < 1900 || annee_p.getValue() > 2100) {
                showError("L'année doit être comprise entre 1900 et 2100.");
                return;
            }

            if (periodicite.getText().trim().isEmpty()) {
                showError("Veuillez saisir la périodicité du magazine.");
                return;
            }

            if (date_de_pub.getText().trim().isEmpty()) {
                showError("Veuillez saisir une date de publication.");
                return;
            }

            if (mot_cles.getText().trim().isEmpty()) {
                showError("Veuillez entrer au moins un mot-clé.");
                return;
            }

            // --- Conversion de la date ---
            LocalDate datePublication;
            try {
                datePublication = LocalDate.parse(date_de_pub.getText().trim());
            } catch (DateTimeParseException e) {
                showError("Format de date invalide. Utilisez AAAA-MM-JJ.\nExemple : 2024-01-20");
                return;
            }

            // --- Conversion des listes ---
            List<String> auteursList = Arrays.stream(Auteurs.getText().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            List<String> motsClesList = Arrays.stream(mot_cles.getText().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            // --- Création du Magazine ---
            Magazine magazine = new Magazine(
                    0,
                    titre.getText().trim(),
                    auteursList,
                    annee_p.getValue(),
                    editeur.getText().trim(),
                    resume.getText().trim(),
                    catg.getValue(),
                    motsClesList,
                    langue.getValue(),
                    num.getValue(),
                    periodicite.getText().trim(),
                    datePublication);

            documentManager.ajouterDocument(magazine);

            showInfo("Magazine ajouté avec succès !");
            parentController.refreshTable();
            titre.getScene().getWindow().hide();

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
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
