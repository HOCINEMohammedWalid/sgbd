package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.These;
import com.sgeb.sgbd.model.enums.Categorie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AjouterTheseController {

    private DocumentManager documentManager;
    private DocumentsController parentController;

    @FXML
    private TextField Auteurs;

    @FXML
    private Spinner<Integer> annee_p;

    @FXML
    private TextField auteur_p;

    @FXML
    private ChoiceBox<Categorie> catg;

    @FXML
    private TextField date_s;

    @FXML
    private TextField discp;

    @FXML
    private TextField dr;

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
    private TextField tp_acees;

    @FXML
    private TextField univ;

    @FXML
    public void initialize() {

        annee_p.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2100, 2024));

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

            // ----------- VALIDATIONS -----------
            if (titre.getText().trim().isEmpty()) {
                showError("Le titre ne peut pas être vide.");
                return;
            }

            if (Auteurs.getText().trim().isEmpty()) {
                showError("Veuillez entrer au moins un auteur.");
                return;
            }

            if (auteur_p.getText().trim().isEmpty()) {
                showError("Veuillez entrer l'auteur principal.");
                return;
            }

            if (dr.getText().trim().isEmpty()) {
                showError("Veuillez entrer le directeur de recherche.");
                return;
            }

            if (univ.getText().trim().isEmpty()) {
                showError("Veuillez entrer l'université.");
                return;
            }

            if (discp.getText().trim().isEmpty()) {
                showError("Veuillez entrer la discipline.");
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
                showError("Veuillez entrer l'éditeur.");
                return;
            }

            if (resume.getText().trim().isEmpty()) {
                showError("Veuillez saisir un résumé.");
                return;
            }

            if (tp_acees.getText().trim().isEmpty()) {
                showError("Veuillez entrer le type d'accès.");
                return;
            }

            if (mot_cles.getText().trim().isEmpty()) {
                showError("Veuillez entrer au moins un mot-clé.");
                return;
            }

            if (annee_p.getValue() < 1900 || annee_p.getValue() > 2100) {
                showError("L'année doit être comprise entre 1900 et 2100.");
                return;
            }

            if (date_s.getText().trim().isEmpty()) {
                showError("Veuillez entrer la date de soutenance.");
                return;
            }

            // ---------- PARSE DATE ----------
            LocalDate dateSoutenance;
            try {
                dateSoutenance = LocalDate.parse(date_s.getText().trim());
            } catch (DateTimeParseException e) {
                showError("Format de date invalide. Utilisez AAAA-MM-JJ.\nExemple : 2024-06-15");
                return;
            }

            // ---------- LISTES ----------
            List<String> auteursList = Arrays.stream(Auteurs.getText().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            List<String> motsClesList = Arrays.stream(mot_cles.getText().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            // ----------- CREATION OBJET THESE -----------
            These these = new These(
                    0,
                    titre.getText().trim(),
                    auteursList,
                    annee_p.getValue(),
                    editeur.getText().trim(),
                    resume.getText().trim(),
                    catg.getValue(),
                    motsClesList,
                    langue.getValue(),
                    auteur_p.getText().trim(),
                    dr.getText().trim(),
                    univ.getText().trim(),
                    discp.getText().trim(),
                    dateSoutenance,
                    tp_acees.getText().trim());

            documentManager.ajouterDocument(these);

            showInfo("Thèse ajoutée avec succès !");
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
