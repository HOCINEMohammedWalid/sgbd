package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.exception.AdherentInexistantException;
import com.sgeb.sgbd.model.exception.ModificationAdherentException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AdherentsController implements Initializable {

    // Manager
    private AdherentManager adherentManager;
    private ObservableList<Adherent> adherentData = FXCollections.observableArrayList();

    // ==== FXML TableView et Colonnes ====
    @FXML
    private TableView<Adherent> TableMvc;

    @FXML
    private TableColumn<Adherent, Integer> IDCol;
    @FXML
    private TableColumn<Adherent, String> NomCol;
    @FXML
    private TableColumn<Adherent, String> PrenomCol;
    @FXML
    private TableColumn<Adherent, String> emailCol;
    @FXML
    private TableColumn<Adherent, String> TelephoneCol;
    @FXML
    private TableColumn<Adherent, String> AdressCol;
    @FXML
    private TableColumn<Adherent, LocalDate> dateCol;
    @FXML
    private TableColumn<Adherent, String> StatutCol;
    @FXML
    private TableColumn<Adherent, Void> DetailsCol; // Colonne pour les actions (Détails/Supprimer)

    // ==== FXML Champs de Recherche ====
    // ... (Champs de recherche inchangés)
    @FXML
    private TextField id_search;
    @FXML
    private TextField nom_search;
    @FXML
    private TextField prenom_search;
    @FXML
    private TextField email_search;
    @FXML
    private TextField tel_search;
    @FXML
    private TextField adress_search;
    @FXML
    private TextField status_search;
    @FXML
    private TextField date_search;

    // =============================================
    // INJECTION DU MANAGER & INITIALIZATION
    // =============================================

    // Dans AdherentsController.java

    public void setManager(AdherentManager adherentManager) {
        this.adherentManager = adherentManager;

        refreshTable();

        setupSearchListeners();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerColonnes();
        ajouterActionsAuxColonnes();

        TableMvc.setItems(adherentData);
    }

    // =============================================
    // REMPLIR & CONFIGURER LA TABLE
    // =============================================

    public void refreshTable() {
        if (adherentManager != null) {
            try {
                adherentData.setAll(adherentManager.listerTous());
            } catch (SQLException e) {
                showError("Erreur BDD", "Erreur lors du chargement des adhérents: " + e.getMessage());
            }
        }
    }

    private void configurerColonnes() {
        IDCol.setCellValueFactory(new PropertyValueFactory<>("idAdherent"));
        NomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        PrenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TelephoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        AdressCol.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        StatutCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatut().toString()));
    }

    // =============================================
    // COLONNE "Détails / Supprimer" (IMPLÉMENTATION)
    // =============================================

    private void ajouterActionsAuxColonnes() {
        // La CellFactory est un CallBack qui crée chaque cellule de la colonne
        Callback<TableColumn<Adherent, Void>, TableCell<Adherent, Void>> cellFactory = param -> new TableCell<Adherent, Void>() {

            // Icônes
            private final ImageView detailsIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/sgeb/sgbd/view/images/details.png")));
            private final ImageView deleteIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/sgeb/sgbd/view/images/bouton-supprimer.png")));

            {
                // Configuration des icônes
                detailsIcon.setFitWidth(18);
                detailsIcon.setFitHeight(18);
                deleteIcon.setFitWidth(18);
                deleteIcon.setFitHeight(18);
                detailsIcon.setStyle("-fx-cursor: hand;");
                deleteIcon.setStyle("-fx-cursor: hand;");

                // Ajout des gestionnaires d'événements
                detailsIcon.setOnMouseClicked(e -> openDetails());
                deleteIcon.setOnMouseClicked(e -> deleteAdherent());
            }

            /**
             * Ouvre la fenêtre de détails de l'adhérent sélectionné.
             */
            private void openDetails() {

                Adherent adherent = getTableView().getItems().get(getIndex());
                if (adherent == null)
                    return;

                // Chemin d'accès au FXML des détails de l'adhérent (à ajuster si besoin)
                final String fxmlPath = "/com/sgeb/sgbd/view/DetailsAdherent.fxml";

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();

                    Object controller = loader.getController();

                    if (controller instanceof DetailsAdherentController) {
                        ((DetailsAdherentController) controller).setAdherent(adherent);
                        ((DetailsAdherentController) controller).setAdherentManager(adherentManager);
                        ((DetailsAdherentController) controller).setParent(AdherentsController.this);
                    } else {
                        System.err.println("Le contrôleur n'implémente pas AdherentDetailsControllerBase.");
                    }

                    Stage stage = new Stage();
                    stage.setTitle("Détails de l'adhérent - " + adherent.getNom() + " " +
                            adherent.getPrenom());
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.showAndWait();

                } catch (IOException e) {
                    showError("Erreur d'Interface", "Erreur lors du chargement : " + fxmlPath);
                    e.printStackTrace();
                }

            }

            /**
             * Supprime l'adhérent après confirmation.
             */
            private void deleteAdherent() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size())
                    return;

                Adherent adh = getTableView().getItems().get(index);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Confirmation de Suppression");
                alert.setContentText("Voulez-vous vraiment supprimer l'adhérent : " +
                        adh.getNom() + " " + adh.getPrenom() + " ?");

                if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    try {
                        // Supposons que AdherentManager ait une méthode supprimerAdherent(int id)
                        adherentManager.deleteAdherent(adh.getIdAdherent());
                        refreshTable(); // Rafraîchit la table après succès
                    } catch (SQLException e) {
                        showError("Erreur SQL", "Impossible de supprimer l'adhérent (BDD) : " +
                                e.getMessage());
                    } catch (Exception e) {
                        showError("Erreur", "Une erreur inattendue est survenue : " +
                                e.getMessage());
                    }
                }

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Conteneur pour aligner les icônes
                    HBox box = new HBox(detailsIcon, deleteIcon);
                    box.setSpacing(8);
                    setGraphic(box);
                }
            }
        };

        DetailsCol.setCellFactory(cellFactory);
    }

    @FXML
    void ajoute(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgeb/sgbd/view/AjouterAdherent.fxml"));
            Parent root = loader.load();

            AjouterAdherentController controller = loader.getController();
            controller.setManager(adherentManager);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter document");
            stage.showAndWait(); // showAndWait pour bloquer jusqu'à la fermeture

            refreshTable();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur d'interface",
                    "Impossible de charger la fenêtre d'ajout d'adhérent.");
        }

    }

    // Dans AdherentsController.java

    private FilteredList<Adherent> filteredData; // Déclaration au niveau de la classe pour réutilisation

    private void setupSearchListeners() {
        // Créez ou réinitialisez la FilteredList ici, en la liant à adherentData
        if (filteredData == null) {
            filteredData = new FilteredList<>(adherentData, a -> true);
        } else {
            // Au cas où setupSearchListeners est appelé plusieurs fois (moins courant)
            filteredData.setPredicate(a -> true);
        }

        // Assurez-vous que la SortedList est liée à la FilteredList
        SortedList<Adherent> sorted = new SortedList<>(filteredData);
        sorted.comparatorProperty().bind(TableMvc.comparatorProperty());
        TableMvc.setItems(sorted);

        // La logique de filtrage est la même
        Runnable filter = () -> applyFilter(filteredData);

        // Ajout des listeners
        id_search.textProperty().addListener((a, b, c) -> filter.run());
        nom_search.textProperty().addListener((a, b, c) -> filter.run());
        prenom_search.textProperty().addListener((a, b, c) -> filter.run());
        email_search.textProperty().addListener((a, b, c) -> filter.run());
        tel_search.textProperty().addListener((a, b, c) -> filter.run());

        // Ajout des listeners pour les autres champs de recherche (si vous souhaitez
        // les utiliser)
        date_search.textProperty().addListener((a, b, c) -> filter.run());
        status_search.textProperty().addListener((a, b, c) -> filter.run());
        adress_search.textProperty().addListener((a, b, c) -> filter.run());
    }

    // Dans AdherentsController.java

    private void applyFilter(FilteredList<Adherent> filter) {
        String idKey = id_search.getText().toLowerCase();
        String nomKey = nom_search.getText().toLowerCase();
        String prenomKey = prenom_search.getText().toLowerCase();
        String numKey = tel_search.getText().toLowerCase();
        String statutKey = status_search.getText().toLowerCase();

        String adressKey = adress_search.getText().toLowerCase();
        String emailKey = email_search.getText().toLowerCase();
        String dateKey = date_search.getText().toLowerCase();

        filter.setPredicate(adherent -> {

            boolean matchesId = idKey.isEmpty() || String.valueOf(adherent.getIdAdherent()).startsWith(idKey);
            boolean matchesNom = nomKey.isEmpty() || adherent.getNom().toLowerCase().startsWith(nomKey);
            boolean matchesPrenom = prenomKey.isEmpty() || adherent.getPrenom().toLowerCase().startsWith(prenomKey);
            boolean matchesTel = numKey.isEmpty() || adherent.getTelephone().toLowerCase().startsWith(numKey);
            boolean matchesStatut = statutKey.isEmpty()
                    || adherent.getStatut().toString().toLowerCase().startsWith(statutKey);

            boolean matchesadress = adressKey.isEmpty()
                    || adherent.getAdresse().toString().toLowerCase().startsWith(adressKey);
            boolean matchesemail = emailKey.isEmpty() || adherent.getEmail().toLowerCase().startsWith(emailKey);
            boolean matchesdate = dateKey.isEmpty()
                    || adherent.getDateInscription().toString().toLowerCase().contains(dateKey);

            // Tous les critères doivent être remplis (logique AND)
            return matchesId && matchesNom && matchesPrenom && matchesTel && matchesStatut
                    && matchesadress && matchesemail && matchesdate;
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}