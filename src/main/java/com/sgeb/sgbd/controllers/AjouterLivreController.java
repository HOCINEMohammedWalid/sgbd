package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Livre;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.enums.Categorie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AjouterLivreController {

    private DocumentManager documentManager;
    private DocumentsController parentController;

    @FXML
    private TextField Auteurs;

    @FXML
    private Spinner<Integer> annee_p;

    @FXML
    private ChoiceBox<Categorie> catg;

    @FXML
    private TextField collec;

    @FXML
    private TextField editeur;

    @FXML
    private TextField isbn;

    @FXML
    private ChoiceBox<String> langue;

    @FXML
    private TextField mot_cles;

    @FXML
    private Spinner<Integer> nbpages;

    @FXML
    private TextArea resume;

    @FXML
    private TextField titre;

    @FXML
    public void initialize() {
        annee_p.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1400, 2100, 2024));
        nbpages.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 100));

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
            // --- VALIDATIONS ---
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

            if (langue.getValue() == null || langue.getValue().trim().isEmpty()) {
                showError("Veuillez sélectionner une langue.");
                return;
            }

            if (annee_p.getValue() < 1400 || annee_p.getValue() > 2100) {
                showError("L'année n'est pas valide.");
                return;
            }

            if (isbn.getText().trim().isEmpty()) {
                showError("Veuillez entrer un ISBN.");
                return;
            }

            if (!isbn.getText().matches("[0-9\\-]{10,17}")) {
                showError("Format ISBN invalide (10 à 13 chiffres, tirets autorisés).");
                return;
            }

            if (mot_cles.getText().trim().isEmpty()) {
                showError("Veuillez entrer au moins un mot-clé.");
                return;
            }

            if (nbpages.getValue() <= 0) {
                showError("Le nombre de pages doit être positif.");
                return;
            }

            // --- LISTES ---
            List<String> auteursList = Arrays.stream(Auteurs.getText().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            List<String> motsClesList = Arrays.stream(mot_cles.getText().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            // --- CRÉATION OBJET LIVRE ---
            Livre livre = new Livre(
                    0,
                    titre.getText(),
                    auteursList,
                    annee_p.getValue(),
                    editeur.getText(),
                    resume.getText(),
                    catg.getValue(),
                    motsClesList,
                    langue.getValue(),
                    isbn.getText(),
                    nbpages.getValue(),
                    collec.getText());

            documentManager.ajouterDocument(livre);

            showInfo("Livre ajouté avec succès !");
            parentController.refreshTable();
            titre.getScene().getWindow().hide();

        } catch (Exception e) {
            showError("Erreur lors de l'ajout : " + e.getMessage());
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
