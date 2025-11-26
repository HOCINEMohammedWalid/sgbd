package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.Emprunt;
import com.sgeb.sgbd.model.EmpruntManager;
// Assurez-vous d'importer Adherent et Document si ces classes sont dans des packages différents
// import com.sgeb.sgbd.model.Adherent;
// import com.sgeb.sgbd.model.Document; 

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class EmpruntsController implements Initializable {

    // Manager
    private EmpruntManager empruntManager;
    private ObservableList<Emprunt> empruntData = FXCollections.observableArrayList();
    private FilteredList<Emprunt> filteredData;

    // ==== FXML TableView et Colonnes ====
    @FXML
    private TableView<Emprunt> TableEmprunts;
    @FXML
    private TableColumn<Emprunt, Integer> IDCol;
    @FXML
    private TableColumn<Emprunt, String> adherentCol;
    @FXML
    private TableColumn<Emprunt, String> DucumentCol;
    @FXML
    private TableColumn<Emprunt, LocalDate> date_emprtCol;
    @FXML
    private TableColumn<Emprunt, LocalDate> date_retour_prevuCol;
    @FXML
    private TableColumn<Emprunt, Long> retardCol;
    @FXML
    private TableColumn<Emprunt, Void> DetailsCol;
    // Les colonnes d'action doivent être de type <Emprunt, Void> ou castées
    @FXML
    private TableColumn<?, ?> details_adeherentCol;
    @FXML
    private TableColumn<?, ?> details_document;

    // ==== FXML Champs de Recherche et Filtres ====
    @FXML
    private TextField id_search;
    @FXML
    private TextField nom_search;
    @FXML
    private TextField titre_search;
    @FXML
    private TextField date_search;
    @FXML
    private CheckBox empt_encours;
    @FXML
    private CheckBox empt_enrrtard;

    // =============================================
    // INJECTION DU MANAGER & INITIALIZATION
    // =============================================

    public void setManager(EmpruntManager empruntManager) {
        System.err.println(empruntManager);
        this.empruntManager = empruntManager;
        refreshTable();
        // Initialisation du filtre après le chargement des données
        if (filteredData == null) {
            setupSearchAndFilter();
        } else {
            applyFilter(); // Réapplique le filtre par défaut
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerColonnes();
        configurerActionColumns(); // Configure toutes les colonnes d'action
        // La ligne TableEmprunts.setItems(empruntData) est implicitement appelée
        // par la liaison via sortedData dans setupSearchAndFilter(), mais c'est bien de
        // l'avoir ici
        TableEmprunts.setItems(empruntData);
    }

    // =============================================
    // CONFIGURATION DE LA TABLE
    // =============================================

    private void configurerColonnes() {
        IDCol.setCellValueFactory(new PropertyValueFactory<>("IdEmprunt"));

        // Assurez-vous que getNomCompletAdherent() et getDocumentTitre() sont robustes
        adherentCol.setCellValueFactory(new PropertyValueFactory<>("NomCompletAdherent"));
        DucumentCol.setCellValueFactory(new PropertyValueFactory<>("DocumentTitre"));

        date_emprtCol.setCellValueFactory(new PropertyValueFactory<>("DateEmprunt"));
        date_retour_prevuCol.setCellValueFactory(new PropertyValueFactory<>("DateRetourPrevue"));

        retardCol.setCellValueFactory(cellData -> {
            Emprunt emprunt = cellData.getValue();
            LocalDate dateRetourReelle = emprunt.getDateRetourReelle();
            LocalDate dateRetourPrevue = emprunt.getDateRetourPrevue();

            long days = 0L; // Initialisation par défaut à 0 jour de retard

            if (dateRetourPrevue != null) {

                if (dateRetourReelle == null) {
                    // Cas 1 : Emprunt toujours en cours (non rendu)
                    if (dateRetourPrevue.isBefore(LocalDate.now())) {
                        // ... et la date limite est passée
                        days = ChronoUnit.DAYS.between(dateRetourPrevue, LocalDate.now());
                    }
                } else {
                    // Cas 2 : Emprunt rendu
                    if (dateRetourReelle.isAfter(dateRetourPrevue)) {
                        // ... et la date réelle est après la date prévue (il y a eu retard)
                        days = ChronoUnit.DAYS.between(dateRetourPrevue, dateRetourReelle);
                    }
                }
            }

            return new ReadOnlyObjectWrapper<>(Math.max(0, days));
        });
    }

    // Nouvelle méthode pour regrouper la configuration de toutes les colonnes
    // d'action
    private void configurerActionColumns() {
        // Configuration de la colonne de détails de l'Emprunt (celle qui était
        // DetailsCol)
        configurerActionButton(DetailsCol, "Détails Emprunt", this::openEmpruntDetails);

        // Configuration de la colonne de détails de l'Adhérent
        configurerActionButton(
                (TableColumn<Emprunt, Void>) details_adeherentCol,
                "Détails Adhérent",
                emprunt -> {
                    if (emprunt.getAdherent() != null) {
                        openAdherentDetails(emprunt.getAdherent());
                    } else {
                        showAlert("Erreur", "Adhérent non chargé pour cet emprunt.", Alert.AlertType.WARNING);
                    }
                });

        // Configuration de la colonne de détails du Document
        configurerActionButton(
                (TableColumn<Emprunt, Void>) details_document,
                "Détails Document",
                emprunt -> {
                    if (emprunt.getDocument() != null) {
                        openDocumentDetails(emprunt.getDocument());
                    } else {
                        showAlert("Erreur", "Document non chargé pour cet emprunt.", Alert.AlertType.WARNING);
                    }
                });
    }

    // Fonction utilitaire générique pour configurer une colonne de bouton
    private void configurerActionButton(
            TableColumn<Emprunt, Void> column,
            String buttonText,
            java.util.function.Consumer<Emprunt> action) {

        Callback<TableColumn<Emprunt, Void>, TableCell<Emprunt, Void>> cellFactory = param -> new TableCell<Emprunt, Void>() {
            private final Button btn = new Button(buttonText);

            {
                // Style facultatif pour le bouton
                btn.getStyleClass().add("action-button");

                btn.setOnAction(event -> {
                    Emprunt emprunt = getTableView().getItems().get(getIndex());
                    action.accept(emprunt);
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

        column.setCellFactory(cellFactory);
    }

    public void refreshTable() {
        if (empruntManager != null) {
            try {
                System.err.println("Début du chargement des emprunts...");
                empruntData.setAll(empruntManager.listerEmprunts());

                System.out.println("Liste des emprunts chargés: " + empruntData.size());

                if (filteredData != null) {
                    applyFilter();
                }
            } catch (SQLException e) {
                showAlert("Erreur BDD", "Erreur lors du chargement des emprunts: " + e.getMessage(),
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

        // Bind les écouteurs aux TextFields et CheckBoxes pour réappliquer le filtre
        Runnable filter = this::applyFilter;

        id_search.textProperty().addListener((obs, oldV, newV) -> filter.run());
        nom_search.textProperty().addListener((obs, oldV, newV) -> filter.run());
        titre_search.textProperty().addListener((obs, oldV, newV) -> filter.run());
        date_search.textProperty().addListener((obs, oldV, newV) -> filter.run());
        empt_encours.selectedProperty().addListener((obs, oldV, newV) -> filter.run());
        empt_enrrtard.selectedProperty().addListener((obs, oldV, newV) -> filter.run());

        SortedList<Emprunt> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(TableEmprunts.comparatorProperty());
        TableEmprunts.setItems(sortedData);
    }

    @FXML
    void search(KeyEvent event) {
        applyFilter();
    }

    private void applyFilter() {
        String idKey = id_search.getText().trim().toLowerCase();
        String nomKey = nom_search.getText().trim().toLowerCase();
        String titreKey = titre_search.getText().trim().toLowerCase();
        String dateKey = date_search.getText().trim().toLowerCase();

        boolean isEnCoursChecked = empt_encours.isSelected();
        boolean isEnRetardChecked = empt_enrrtard.isSelected();

        filteredData.setPredicate(emprunt -> {
            // --- 1. Filtrage par TextFields (Aucun changement) ---
            boolean matchesId = idKey.isEmpty() || String.valueOf(emprunt.getIdEmprunt()).contains(idKey);
            boolean matchesNom = nomKey.isEmpty() || emprunt.getNomCompletAdherent().toLowerCase().contains(nomKey);
            boolean matchesTitre = titreKey.isEmpty() || emprunt.getDocumentTitre().toLowerCase().contains(titreKey);
            boolean matchesDate = dateKey.isEmpty() || emprunt.getDateEmprunt().toString().contains(dateKey);

            if (!(matchesId && matchesNom && matchesTitre && matchesDate))
                return false;

            // --- 2. Filtrage par CheckBoxes (Conditions cumulatives) ---

            // Détermination du statut de l'emprunt
            boolean estRendu = emprunt.getDateRetourReelle() != null;

            // Retard actuel (si non rendu)
            boolean estActuellementEnRetard = !estRendu &&
                    emprunt.getDateRetourPrevue().isBefore(LocalDate.now());

            // Retard historique (si rendu tardivement)
            boolean etaitRenduEnRetard = estRendu &&
                    emprunt.getDateRetourReelle().isAfter(emprunt.getDateRetourPrevue());

            boolean estEnRetardTotal = estActuellementEnRetard || etaitRenduEnRetard;

            // GESTION DU FILTRE 'EN COURS' (Afficher uniquement ceux qui ne sont pas
            // rendus)
            if (isEnCoursChecked) {
                // Si l'utilisateur veut voir les emprunts en cours, et que celui-ci est rendu,
                // on l'élimine.
                if (estRendu) {
                    return false;
                }
            }

            // GESTION DU FILTRE 'EN RETARD' (Afficher uniquement ceux qui sont ou étaient
            // en retard)
            if (isEnRetardChecked) {
                // Si l'utilisateur veut voir les emprunts en retard, et que celui-ci n'est
                // pas/n'était pas en retard, on l'élimine.
                if (!estEnRetardTotal) {
                    return false;
                }
            }

            // GESTION COMBINÉE (si les deux sont cochés):
            // Si les deux sont cochés, on n'affiche que les emprunts EN COURS ET EN RETARD
            // ACTUEL.
            // Si isEnCoursChecked est vrai, on élimine déjà les rendus.
            // Si isEnRetardChecked est vrai, on élimine déjà ceux qui n'ont pas de retard
            // (actuel ou historique).
            // Le filtre est donc naturellement cumulatif et correct.

            return true;
        });
    }

    // =============================================
    // ACTIONS / LOGIQUE MÉTIER (Méthodes de support)
    // =============================================

    private void openEmpruntDetails(Emprunt emprunt) {

        if (emprunt == null || emprunt.getAdherent() == null || emprunt.getDocument() == null) {
            showAlert("Erreur de données",
                    "L'emprunt sélectionné ne possède pas toutes les informations (Adhérent ou Document).",
                    Alert.AlertType.WARNING);
            return;
        }

        // IMPORTANT : Vérifiez et ajustez le chemin FXML si nécessaire
        String fxmlPath = "/com/sgeb/sgbd/view/DetailsEmprunt.fxml";
        loadDetailsWindow(emprunt, fxmlPath);
    }

    private void loadDetailsWindow(Emprunt emprunt, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Récupérer le contrôleur associé au fichier FXML chargé
            Object controller = loader.getController();

            // Vérifier si le contrôleur est de type DetailsEmpruntController
            if (controller instanceof DetailsEmpruntController) {
                // Initialisation du contrôleur de détails
                ((DetailsEmpruntController) controller).setEmprunt(emprunt, empruntManager, EmpruntsController.this);

                // Création de la fenêtre
                Stage stage = new Stage();
                stage.setTitle("Détails de l'Emprunt: " + emprunt.getDocumentTitre());
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

            } else {
                System.err.println(
                        "Attention : Le contrôleur chargé (" + controller.getClass().getSimpleName() +
                                ") n'est pas un DetailsEmpruntController.");
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la fenêtre : " + fxmlPath);
            e.printStackTrace();
            showAlert("Erreur FXML", "Impossible de charger la fenêtre de détails : " + fxmlPath,
                    Alert.AlertType.ERROR);
        }
    }

    private void openAdherentDetails(Object adherent) {
        // En supposant que 'adherent' est un objet de la classe Adherent
        showAlert("Détails Adhérent", "Ouverture des détails pour l'Adhérent: " + adherent.toString(),
                Alert.AlertType.INFORMATION);
        // Ici, vous devriez charger un nouveau FXML de détails Adhérent et lui passer
        // l'objet.
    }

    private void openDocumentDetails(Object document) {
        // En supposant que 'document' est un objet de la classe Document
        showAlert("Détails Document", "Ouverture des détails pour le Document: " + document.toString(),
                Alert.AlertType.INFORMATION);
        // Ici, vous devriez charger un nouveau FXML de détails Document et lui passer
        // l'objet.
    }

    // =============================================
    // UTILITAIRES
    // =============================================

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}