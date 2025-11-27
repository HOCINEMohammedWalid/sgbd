package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.Document; // Nécessaire pour les détails du document
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
import javafx.util.Callback; // Nécessaire pour la cellule de bouton

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
    @FXML
    private TableColumn<Emprunt, String> DocumentCol;
    @FXML
    private TableColumn<Emprunt, Integer> IDCol;
    @FXML
    private TableView<Emprunt> Mesemprunts;
    @FXML
    private TableColumn<Emprunt, Long> Penalité;

    // Colonnes de date
    @FXML
    private TableColumn<Emprunt, LocalDate> dateEmpruntCol;
    @FXML
    private TableColumn<Emprunt, LocalDate> dateRetourPrevuCol;
    @FXML
    private TableColumn<Emprunt, Object> dateRetourReel; // Type Object pour gérer Date ou String ("— EN COURS —")

    // NOUVELLE COLONNE DE BOUTON (pour les détails du document)
    @FXML
    private TableColumn<Emprunt, Void> DetailDocCol;

    // --- Éléments de recherche FXML ---
    @FXML
    private CheckBox actualSearch;
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
        configurerDetailDocumentButton(); // AJOUT DE LA CONFIGURATION DU BOUTON
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
    // CONFIGURATION DE LA TABLE ET DES BOUTONS
    // =============================================

    private void configurerColonnes() {
        IDCol.setCellValueFactory(new PropertyValueFactory<>("IdEmprunt"));
        DocumentCol.setCellValueFactory(new PropertyValueFactory<>("DocumentTitre"));
        dateEmpruntCol.setCellValueFactory(new PropertyValueFactory<>("DateEmprunt"));
        dateRetourPrevuCol.setCellValueFactory(new PropertyValueFactory<>("DateRetourPrevue"));

        // Affichage clair pour la date de retour réelle
        dateRetourReel.setCellValueFactory(cellData -> {
            LocalDate dateReelle = cellData.getValue().getDateRetourReelle();
            if (dateReelle == null) {
                return new ReadOnlyObjectWrapper<>("— EN COURS —");
            } else {
                return new ReadOnlyObjectWrapper<>(dateReelle);
            }
        });

        // Configuration de la colonne de jours de retard calculée dynamiquement
        Penalité.setCellValueFactory(cellData -> {
            Emprunt emprunt = cellData.getValue();
            long days = calculerJoursRetard(emprunt);
            return new ReadOnlyObjectWrapper<>(days);
        });
    }

    /**
     * Configure la colonne DetailDocCol pour afficher un bouton "Détails Document".
     */
    private void configurerDetailDocumentButton() {
        Callback<TableColumn<Emprunt, Void>, TableCell<Emprunt, Void>> cellFactory = param -> new TableCell<Emprunt, Void>() {
            private final Button btn = new Button("Détails Document");

            {
                btn.setOnAction(event -> {
                    Emprunt emprunt = getTableView().getItems().get(getIndex());
                    openDocumentDetails(emprunt); // Appel de la méthode d'action
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        };

        DetailDocCol.setCellFactory(cellFactory);
    }

    /**
     * Ouvre les détails du document associé à l'emprunt.
     */
    private void openDocumentDetails(Emprunt emprunt) {
        // Tente d'utiliser l'objet Document déjà chargé, sinon tente de le charger via
        // le Manager
        Document document = emprunt.getDocument();

        showAlert("Détails Document", "Ouverture des détails pour le Document: " + document.toString(),
                Alert.AlertType.INFORMATION);

    }

    // =============================================
    // LOGIQUE DE DONNÉES ET FILTRAGE
    // =============================================

    public void refreshTable() {
        if (empruntManager != null && adherent != null) {
            try {
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

    private long calculerJoursRetard(Emprunt emprunt) {
        LocalDate datePrevue = emprunt.getDateRetourPrevue();
        LocalDate dateReelle = emprunt.getDateRetourReelle();
        long days = 0L;

        if (datePrevue != null) {
            if (dateReelle == null) {
                if (datePrevue.isBefore(LocalDate.now())) {
                    days = ChronoUnit.DAYS.between(datePrevue, LocalDate.now());
                }
            } else {
                if (dateReelle.isAfter(datePrevue)) {
                    days = ChronoUnit.DAYS.between(datePrevue, dateReelle);
                }
            }
        }
        return Math.max(0, days);
    }

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

            // 1. Filtrage par TextFields (avec vérification de robustesse)
            boolean matchesId = idKey.isEmpty() || String.valueOf(emprunt.getIdEmprunt()).contains(idKey);

            boolean matchesTitre = titreKey.isEmpty()
                    || (emprunt.getDocumentTitre() != null
                            && emprunt.getDocumentTitre().toLowerCase().contains(titreKey));

            boolean matchesDate = dateKey.isEmpty()
                    || (emprunt.getDateEmprunt() != null && emprunt.getDateEmprunt().toString().contains(dateKey));

            if (!(matchesId && matchesTitre && matchesDate))
                return false;

            // 2. Filtrage par CheckBoxes
            boolean estRendu = emprunt.getDateRetourReelle() != null;
            long joursRetard = calculerJoursRetard(emprunt);

            // Filtre "Actuel"
            if (isActual && estRendu) {
                return false;
            }

            // Filtre "Pénalité" (non payée)
            if (hasPenalite) {
                // Doit avoir du retard ET ne doit PAS être payée
                if (!(joursRetard > 0 && !emprunt.isPayee())) {
                    return false;
                }
            }

            return true;
        });
    }

    // =============================================
    // UTILITAIRES
    // =============================================

    @FXML
    void ajouter(ActionEvent event) {
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