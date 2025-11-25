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
        auteursCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAuteurs().toString()));
        Annee_de_puplicationCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAnneePublication()).asObject());
        editeurCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEditeur()));
        categorieCol
                .setCellValueFactory(
                        c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategorie().toString()));
        langueCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLangue()));
        DispoCol.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(true ? "Oui" : "Non"));
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
                /*
                 * Document doc = getTableView().getItems().get(getIndex());
                 * try {
                 * FXMLLoader loader = new
                 * FXMLLoader(getClass().getResource("/view/DetailsDocument.fxml"));
                 * Parent root = loader.load();
                 * DetailsDocumentController controller = loader.getController();
                 * controller.setDocument(doc);
                 * Stage stage = new Stage();
                 * stage.setScene(new Scene(root));
                 * stage.setTitle("Détails du document");
                 * stage.show();
                 * } catch (IOException ex) {
                 * ex.printStackTrace();
                 * }
                 */
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
        String auteursKey = auteurs_search.getText().toLowerCase();
        String anKey = annPub_search.getText().toLowerCase();
        String editKey = editeur_search.getText().toLowerCase();
        String catKey = catg_search.getText().toLowerCase();
        String langKey = lang_search.getText().toLowerCase();
        boolean dispoKey = dispo.isSelected();

        filter.setPredicate(doc -> {
            boolean matchesId = String.valueOf(doc.getIdDocument()).startsWith(idKey);
            boolean matchesTitre = doc.getTitre().toLowerCase().contains(titreKey);
            boolean matchesType = doc.getTypeDocument().toString().toLowerCase().contains(typeKey);
            boolean matchesAuteurs = doc.getAuteurs().toString().toLowerCase().contains(auteursKey);
            boolean matchesAn = String.valueOf(doc.getAnneePublication()).startsWith(anKey);
            boolean matchesEdit = doc.getEditeur().toLowerCase().contains(editKey);
            boolean matchesCat = doc.getCategorie().toString().toLowerCase().contains(catKey);
            boolean matchesLang = doc.getLangue().toLowerCase().contains(langKey);
            boolean matchesDispo = !dispoKey || true;
            return matchesId && matchesTitre && matchesType &&
                    matchesAuteurs && matchesAn && matchesEdit &&
                    matchesCat && matchesLang && matchesDispo;
        });
    }
}
