package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.Emprunt;
import com.sgeb.sgbd.model.EmpruntManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class MesEmpruntsController implements Initializable {

    // --- Champs de données et de gestion ---
    private Adherent adherent;
    private EmpruntManager empruntManager;
    private ObservableList<Emprunt> empruntData = FXCollections.observableArrayList();
    private FilteredList<Emprunt> filteredData;

    // --- Colonnes FXML ---
    // REMARQUE : Les colonnes d'action (Détails/Paiement, Document) ont été
    // retirées.
    @FXML
    private TableColumn<Emprunt, String> DocumentCol;
    @FXML
    private TableColumn<Emprunt, Integer> IDCol;
    @FXML
    private TableView<Emprunt> Mesemprunts;
    @FXML
    private TableColumn<Emprunt, Long> Penalité;

    // --- Éléments de recherche FXML ---
    @FXML
    private CheckBox actualSearch;
    @FXML
    private TableColumn<Emprunt, LocalDate> dateEmpruntCol;
    @FXML
    private TableColumn<Emprunt, LocalDate> dateRetourPrevuCol;
    @FXML
    private TableColumn<Emprunt, LocalDate> dateRetourReel;
    @FXML
    private TextField date_empruntSearch;
    @FXML
    private TextField id_search;
    @FXML
    private TextField nom_sdocSearchearch;
    @FXML
    private CheckBox penaliteSearch;

    // =============================================
    // INITIALISATION
    // =============================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerColonnes();
        Mesemprunts.setItems(empruntData);
    }

    /**
     * Méthode appelée par le contrôleur parent pour injecter le manager et
     * l'adhérent.
     */
    public void setDependencies(EmpruntManager empruntManager, Adherent adherent) {
        this.empruntManager = empruntManager;
        this.adherent = adherent;

        refreshTable();

        if (filteredData == null) {
            setupSearchAndFilter();
        } else {
            applyFilter();
        }
    }

    // =============================================
    // CONFIGURATION DE LA TABLE
    // =============================================

    private void configurerColonnes() {
        IDCol.setCellValueFactory(new PropertyValueFactory<>("IdEmprunt"));
        DocumentCol.setCellValueFactory(new PropertyValueFactory<>("DocumentTitre"));
        dateEmpruntCol.setCellValueFactory(new PropertyValueFactory<>("DateEmprunt"));
        dateRetourPrevuCol.setCellValueFactory(new PropertyValueFactory<>("DateRetourPrevue"));
        dateRetourReel.setCellValueFactory(new PropertyValueFactory<>("DateRetourReelle"));

        // Configuration de la colonne de jours de retard calculée dynamiquement
        Penalité.setCellValueFactory(cellData -> {
            Emprunt emprunt = cellData.getValue();
            LocalDate datePrevue = emprunt.getDateRetourPrevue();
            LocalDate dateReelle = emprunt.getDateRetourReelle();
            long days = 0L;

            if (datePrevue != null) {
                if (dateReelle == null) {
                    // Cas 1 : Emprunt en cours et en retard (comparaison avec aujourd'hui)
                    if (datePrevue.isBefore(LocalDate.now())) {
                        days = ChronoUnit.DAYS.between(datePrevue, LocalDate.now());
                    }
                } else {
                    // Cas 2 : Emprunt rendu en retard (retard fixe par rapport à la date réelle)
                    if (dateReelle.isAfter(datePrevue)) {
                        days = ChronoUnit.DAYS.between(datePrevue, dateReelle);
                    }
                }
            }
            return new ReadOnlyObjectWrapper<>(Math.max(0, days));
        });
    }

    // La méthode configurerActionColumns est retirée/vidée car aucun bouton n'est
    // souhaité.

    // =============================================
    // GESTION DES DONNÉES ET RAFRAÎCHISSEMENT
    // =============================================

    public void refreshTable() {
        if (empruntManager != null && adherent != null) {
            try {
                // IMPORTANT : utilise la méthode listerEmpruntsParAdherent
                empruntData.setAll(empruntManager.listerEmpruntsParAdherent(adherent.getIdAdherent()));

                if (filteredData != null) {
                    applyFilter();
                }
            } catch (SQLException e) {
                showAlert("Erreur BDD", "Erreur lors du chargement de vos emprunts: " + e.getMessage(),
                        Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    // =============================================
    // RECHERCHE ET FILTRAGE
    // =============================================

    private void setupSearchAndFilter() {
        filteredData = new FilteredList<>(empruntData, p -> true);

        Runnable filter = this::applyFilter;

        id_search.textProperty().addListener((obs, oldV, newV) -> filter.run());
        nom_sdocSearchearch.textProperty().addListener((obs, oldV, newV) -> filter.run());
        date_empruntSearch.textProperty().addListener((obs, oldV, newV) -> filter.run());
        actualSearch.selectedProperty().addListener((obs, oldV, newV) -> filter.run());
        penaliteSearch.selectedProperty().addListener((obs, oldV, newV) -> filter.run());

        SortedList<Emprunt> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(Mesemprunts.comparatorProperty());
        Mesemprunts.setItems(sortedData);
    }

    @FXML
    void search(KeyEvent event) {
        applyFilter();
    }

    private void applyFilter() {
        String idKey = id_search.getText().trim().toLowerCase();
        String titreKey = nom_sdocSearchearch.getText().trim().toLowerCase();
        String dateKey = date_empruntSearch.getText().trim().toLowerCase();

        boolean isActual = actualSearch.isSelected();
        boolean hasPenalite = penaliteSearch.isSelected();

        filteredData.setPredicate(emprunt -> {

            // 1. Filtrage par TextFields
            boolean matchesId = idKey.isEmpty() || String.valueOf(emprunt.getIdEmprunt()).contains(idKey);
            boolean matchesTitre = titreKey.isEmpty() || emprunt.getDocumentTitre().toLowerCase().contains(titreKey);
            boolean matchesDate = dateKey.isEmpty() || emprunt.getDateEmprunt().toString().contains(dateKey);

            if (!(matchesId && matchesTitre && matchesDate))
                return false;

            // 2. Filtrage par CheckBoxes
            boolean estRendu = emprunt.getDateRetourReelle() != null;

            // Filtre "Actuel" (équivalent à "En cours")
            if (isActual && estRendu) {
                return false;
            }

            // Filtre "Pénalité" (Afficher ceux qui ont une pénalité non payée)
            if (hasPenalite) {
                if (!(emprunt.getPenalite() > 0 && !emprunt.isPayee())) {
                    return false;
                }
            }

            return true;
        });
    }

    // =============================================
    // ACTIONS
    // =============================================

    @FXML
    void ajouter(ActionEvent event) {
        // L'adhérent n'est pas censé "ajouter" un emprunt depuis cette vue.
        showAlert("Fonctionnalité", "L'ajout d'emprunt se fait via la liste des documents.",
                Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}