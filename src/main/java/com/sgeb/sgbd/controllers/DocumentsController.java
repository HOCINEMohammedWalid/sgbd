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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;
import com.sgeb.sgbd.model.exception.DocumentInexistantException;
import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.Document;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DocumentsController implements Initializable {

    private DocumentManager documentManager;

    private AdherentManager adherentManager;
    private EmpruntManager empruntManager;

    public void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM) {
        this.documentManager = docM;
        this.adherentManager = adhM;
        this.empruntManager = empM;
        refreshTable(); // ou autre initialisation
    }

    public void setManagers(DocumentManager docM) {
        this.documentManager = docM;
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
        // Dans DocumentsController.java -> configurerColonnes()
        auteursCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(String.join(", ", c.getValue().getAuteurs())));
        Annee_de_puplicationCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAnneePublication()).asObject());
        editeurCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEditeur()));
        categorieCol
                .setCellValueFactory(
                        c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategorie().toString()));
        langueCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLangue()));
        DispoCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isDispo() ? "Oui" : "Non"));
    }

    // =============================================
    // COLONNE "Détails / Supprimer"
    // =============================================
    private void ajouterActionsAuxColonnes() {
        Callback<TableColumn<Document, Void>, TableCell<Document, Void>> cellFactory = param -> new TableCell<Document, Void>() {

            private final ImageView detailsIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/sgeb/sgbd/view/images/details.png")));
            private final ImageView deleteIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/sgeb/sgbd/view/images/bouton-supprimer.png")));

            {
                detailsIcon.setFitWidth(18);
                detailsIcon.setFitHeight(18);
                deleteIcon.setFitWidth(18);
                deleteIcon.setFitHeight(18);
                detailsIcon.setStyle("-fx-cursor: hand;");
                deleteIcon.setStyle("-fx-cursor: hand;");

                detailsIcon.setOnMouseClicked(e -> openDetails());
                deleteIcon.setOnMouseClicked(e -> deleteDocument());
            }

            private void openDetails() {
                Document document = getTableView().getItems().get(getIndex());
                if (document == null || document.getTypeDocument() == null)
                    return;

                String fxmlPath = document.getTypeDocument().getFxmlPath();
                loadDetailsWindow(document, fxmlPath);
            }

            private void loadDetailsWindow(Document document, String fxmlPath) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();

                    // Récupérer le contrôleur associé au fichier FXML chargé
                    Object controller = loader.getController();

                    // Vérifier si le contrôleur implémente l'interface DetailsControllerBase
                    if (controller instanceof DetailsControllerBase) {
                        ((DetailsControllerBase) controller).setDocument(document);

                        ((DetailsControllerBase) controller).setDocumentManager(documentManager);
                        ((DetailsControllerBase) controller).setParent(DocumentsController.this);
                    } else {
                        System.err.println(
                                "Attention : Le contrôleur " + controller.getClass().getSimpleName() +
                                        " n'implémente pas DetailsControllerBase. Impossible de transmettre le document.");
                    }

                    // Création de la fenêtre
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

            private void deleteDocument() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size())
                    return;
                Document doc = getTableView().getItems().get(index);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Supprimer ?");
                alert.setContentText("Supprimer le document : " + doc.getTitre() + " ?");

                if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    try {
                        documentManager.supprimerDocument(doc.getIdDocument());
                        refreshTable();
                    } catch (SQLException e) {
                        showError("Erreur SQL", e.getMessage());
                    } catch (DocumentInexistantException e) {
                        showError("Document inexistant", e.getMessage());
                    }
                }
            }

            private void showError(String title, String message) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(detailsIcon, deleteIcon);
                    box.setSpacing(8);
                    setGraphic(box);
                }
            }
        };

        DetailsCol.setCellFactory(cellFactory);
    }

    // =============================================
    // AJOUTER NOUVEAU
    // =============================================
    @FXML
    void ajouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgeb/sgbd/view/AjouteDocument.fxml"));
            Parent root = loader.load();
            AjouterDocumentController controller = loader.getController();
            controller.setManager(documentManager);
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter document");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // =============================================
    // RECHERCHE
    // =============================================
    @FXML
    void search(KeyEvent event) {
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
        String auteursKey = auteurs_search.getText().toLowerCase(); // La clé de recherche de l'utilisateur
        String anKey = annPub_search.getText().toLowerCase();
        String editKey = editeur_search.getText().toLowerCase();
        String catKey = catg_search.getText().toLowerCase();
        String langKey = lang_search.getText().toLowerCase();
        boolean dispoKey = dispo.isSelected();

        filter.setPredicate(doc -> {
            // --- Logique "starts with" pour tous les champs ---

            // Les vérifications de chaînes simples restent les mêmes (elles fonctionnent)
            boolean matchesId = idKey.isEmpty() || String.valueOf(doc.getIdDocument()).startsWith(idKey);
            boolean matchesTitre = titreKey.isEmpty() || doc.getTitre().toLowerCase().startsWith(titreKey);
            boolean matchesType = typeKey.isEmpty()
                    || doc.getTypeDocument().toString().toLowerCase().startsWith(typeKey);
            boolean matchesAn = anKey.isEmpty() || String.valueOf(doc.getAnneePublication()).startsWith(anKey);
            boolean matchesEdit = editKey.isEmpty() || doc.getEditeur().toLowerCase().startsWith(editKey);
            boolean matchesCat = catKey.isEmpty() || doc.getCategorie().toString().toLowerCase().startsWith(catKey);
            boolean matchesLang = langKey.isEmpty() || doc.getLangue().toLowerCase().startsWith(langKey);

            // --- CORRECTION pour les Auteurs ---
            boolean matchesAuteurs;
            if (auteursKey.isEmpty()) {
                // Si la clé de recherche est vide, on considère que ça correspond à tout.
                matchesAuteurs = true;
            } else {
                // On vérifie si AU MOINS UN auteur dans la liste du document commence par la
                // clé de recherche.
                matchesAuteurs = doc.getAuteurs().stream()
                        .anyMatch(auteur -> auteur.toLowerCase().startsWith(auteursKey));
            }

            // Logique de Disponibilité
            boolean estDisponible = doc.isDispo();
            boolean matchesDispo = !dispoKey || estDisponible;

            // Retourne le résultat combiné
            return matchesId && matchesTitre && matchesType &&
                    matchesAuteurs && matchesAn && matchesEdit &&
                    matchesCat && matchesLang && matchesDispo;
        });
    }
}
