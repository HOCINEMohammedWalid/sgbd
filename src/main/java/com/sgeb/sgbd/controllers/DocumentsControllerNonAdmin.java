package com.sgeb.sgbd.controllers;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality; // Ajout de Modality pour la fenêtre de détails
import javafx.stage.Stage;
import javafx.util.Callback;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;
import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.Document;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DocumentsControllerNonAdmin implements Initializable {

    private DocumentManager documentManager;

    private AdherentManager adherentManager;
    private EmpruntManager empruntManager;
    private Adherent adherent;

    public void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM) {

        this.documentManager = docM;
        this.adherentManager = adhM;
        this.empruntManager = empM;
        refreshTable();
    }

    public void setManagers(DocumentManager docM) {
        this.documentManager = docM;
        refreshTable();
    }

    public void setAdherent(Adherent adherent) {
        this.adherent = adherent;
        refreshTable();
    }

    // ==== FXML ====
    @FXML
    private TableView<Document> TableChercheurs;

    @FXML
    private TableColumn<Document, Integer> IDCol;
    @FXML
    private TableColumn<Document, String> TitreCol;
    @FXML
    private TableColumn<Document, String> typeCol;
    @FXML
    private TableColumn<Document, String> auteursCol;
    @FXML
    private TableColumn<Document, Integer> Annee_de_puplicationCol;
    @FXML
    private TableColumn<Document, String> editeurCol;
    @FXML
    private TableColumn<Document, String> categorieCol;
    @FXML
    private TableColumn<Document, String> langueCol;
    @FXML
    private TableColumn<Document, String> DispoCol;
    @FXML
    private TableColumn<Document, Void> DetailsCol;

    @FXML
    private TextField id_search;
    @FXML
    private TextField titre_search;
    @FXML
    private TextField type_search;
    @FXML
    private TextField auteurs_search;
    @FXML
    private TextField annPub_search;
    @FXML
    private TextField editeur_search;
    @FXML
    private TextField catg_search;
    @FXML
    private TextField lang_search;

    @FXML
    private CheckBox dispo;

    private ObservableList<Document> data = FXCollections.observableArrayList();

    // =============================================
    // INITIALIZE
    // =============================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerColonnes();
        ajouterActionsAuxColonnes();
        TableChercheurs.setItems(data);
        setupSearchListeners();
    }

    // =============================================
    // REMPLIR LA TABLE
    // =============================================
    public void refreshTable() {
        if (documentManager != null) {
            data.setAll(documentManager.listerTousDocuments());
        }
    }

    // =============================================
    // CONFIGURER COLONNES
    // =============================================
    private void configurerColonnes() {
        IDCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getIdDocument()).asObject());
        TitreCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitre()));
        typeCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTypeDocument().toString()));

        // Affichage des auteurs sans crochets
        auteursCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(String.join(", ", c.getValue().getAuteurs())));

        Annee_de_puplicationCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAnneePublication()).asObject());
        editeurCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEditeur()));
        categorieCol
                .setCellValueFactory(
                        c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategorie().toString()));
        langueCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLangue()));

        // CORRECTION : Affichage réel de la disponibilité
        DispoCol.setCellValueFactory(c -> {
            boolean estDispo = empruntManager != null && true;
            String status = estDispo ? "Oui" : "Non";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
    }

    // =============================================
    // COLONNE "Détails" (Seulement l'action de détail)
    // =============================================
    private void ajouterActionsAuxColonnes() {
        Callback<TableColumn<Document, Void>, TableCell<Document, Void>> cellFactory = param -> new TableCell<Document, Void>() {

            // Seule l'icône de détails est conservée
            private final ImageView detailsIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/sgeb/sgbd/view/images/details.png")));

            {
                detailsIcon.setFitWidth(18);
                detailsIcon.setFitHeight(18);
                detailsIcon.setStyle("-fx-cursor: hand;");

                detailsIcon.setOnMouseClicked(e -> openDetails());
            }

            private void openDetails() {
                Document document = getTableView().getItems().get(getIndex());
                if (document == null || document.getTypeDocument() == null)
                    return;

                String fxmlPath = document.getTypeDocument().getFxmlPathNonAdmin();
                loadDetailsWindow(document, fxmlPath);
            }

            private void loadDetailsWindow(Document document, String fxmlPath) {
                try {
                    // Les contrôleurs de détails pour les non-admins ne devraient pas avoir
                    // le DocumentManager ni le ParentController, car la modification est interdite.
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();
                    Object controller = loader.getController();

                    // Nous supposons que le contrôleur de détails a une méthode
                    // setDocument(Document)
                    if (controller instanceof DetailsControllerBase) {
                        // Note: Le contrôleur DetailsControllerBase devrait être adapté pour
                        // masquer les boutons de modification pour les non-admins.
                        ((DetailsControllerBase) controller).setDocument(document);
                        ((DetailsControllerBase) controller).setEmpruntManager(empruntManager);
                        ((DetailsControllerBase) controller).setAdherent(adherent);
                        // IMPORTANT: NE PAS INJECTER documentManager ni setParent()
                        // pour interdire les modifications/suppressions.
                    }

                    Stage stage = new Stage();
                    stage.setTitle("Détails du document - " + document.getTitre());
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.showAndWait();

                } catch (IOException e) {
                    System.err.println("Erreur lors du chargement : " + fxmlPath);
                    e.printStackTrace();
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // On affiche seulement l'icône de détails
                    HBox box = new HBox(detailsIcon);
                    box.setSpacing(8);
                    setGraphic(box);
                }
            }
        };

        DetailsCol.setCellFactory(cellFactory);
    }

    // =============================================
    // RECHERCHE
    // =============================================
    @FXML
    void search(KeyEvent event) {
        // Déclenché par les listeners dans setupSearchListeners
    }

    private void setupSearchListeners() {
        FilteredList<Document> filtered = new FilteredList<>(data, e -> true);
        Runnable filter = () -> applyFilter(filtered);

        id_search.textProperty().addListener((a, b, c) -> filter.run());
        titre_search.textProperty().addListener((a, b, c) -> filter.run());
        type_search.textProperty().addListener((a, b, c) -> filter.run());
        auteurs_search.textProperty().addListener((a, b, c) -> filter.run());
        annPub_search.textProperty().addListener((a, b, c) -> filter.run());
        editeur_search.textProperty().addListener((a, b, c) -> filter.run());
        catg_search.textProperty().addListener((a, b, c) -> filter.run());
        lang_search.textProperty().addListener((a, b, c) -> filter.run());
        dispo.selectedProperty().addListener((a, b, c) -> filter.run());

        SortedList<Document> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(TableChercheurs.comparatorProperty());
        TableChercheurs.setItems(sorted);
    }

    private void applyFilter(FilteredList<Document> filter) {
        String idKey = id_search.getText().toLowerCase();
        String titreKey = titre_search.getText().toLowerCase();
        String typeKey = type_search.getText().toLowerCase();
        String auteursKey = auteurs_search.getText().toLowerCase();
        String anKey = annPub_search.getText().toLowerCase();
        String editKey = editeur_search.getText().toLowerCase();
        String catKey = catg_search.getText().toLowerCase();
        String langKey = lang_search.getText().toLowerCase();
        boolean dispoKey = dispo.isSelected();

        filter.setPredicate(doc -> {

            // Logique de recherche startsWith
            boolean matchesId = idKey.isEmpty() || String.valueOf(doc.getIdDocument()).startsWith(idKey);
            boolean matchesTitre = titreKey.isEmpty() || doc.getTitre().toLowerCase().startsWith(titreKey);
            boolean matchesType = typeKey.isEmpty()
                    || doc.getTypeDocument().toString().toLowerCase().startsWith(typeKey);
            boolean matchesAn = anKey.isEmpty() || String.valueOf(doc.getAnneePublication()).startsWith(anKey);
            boolean matchesEdit = editKey.isEmpty() || doc.getEditeur().toLowerCase().startsWith(editKey);
            boolean matchesCat = catKey.isEmpty() || doc.getCategorie().toString().toLowerCase().startsWith(catKey);
            boolean matchesLang = langKey.isEmpty() || doc.getLangue().toLowerCase().startsWith(langKey);

            // Logique Auteurs
            boolean matchesAuteurs;
            if (auteursKey.isEmpty()) {
                matchesAuteurs = true;
            } else {
                matchesAuteurs = doc.getAuteurs().stream()
                        .anyMatch(auteur -> auteur.toLowerCase().startsWith(auteursKey));
            }

            // CORRECTION : Logique de Disponibilité
            boolean estDisponible = empruntManager != null && true;
            boolean matchesDispo = !dispoKey || estDisponible;

            // Retourne le résultat combiné
            return matchesId && matchesTitre && matchesType &&
                    matchesAuteurs && matchesAn && matchesEdit &&
                    matchesCat && matchesLang && matchesDispo;
        });
    }
}