package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager;

import java.sql.SQLException;

import com.sgeb.sgbd.model.ArticleUniversitaire;
import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.exception.DocumentInexistantException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Arrays; // Ajouté pour la conversion d'enum si nécessaire, mais non utilisé ici.
import java.util.List;

public class DetailsArticleUnivController implements DetailsControllerBase {

    @FXML
    private Button Anuller;
    @FXML
    private TextField Auteurs;

    @FXML
    private Spinner<Integer> annee_p;

    @FXML
    private ChoiceBox<Categorie> catg;

    @FXML
    private TextField doi;

    @FXML
    private TextField editeur;

    @FXML
    private ChoiceBox<String> langue;

    @FXML
    private TextField mot_cles;

    @FXML
    private Spinner<Integer> nb_pages;

    @FXML
    private Spinner<Integer> pages_end;

    @FXML
    private Spinner<Integer> pages_start;

    @FXML
    private TextArea resume;

    @FXML
    private TextField titre;

    @FXML
    private TextField titre_revue;

    @FXML
    private Button update;

    @FXML
    private Spinner<Integer> volume;

    private Document document;
    private DocumentManager documentManager;

    @FXML
    public void initialize() {
        // --- Configuration des Spinners ---
        if (annee_p != null) {
            annee_p.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1800, 2100, 2025));
        }
        if (pages_start != null) {
            // Utilisation de la valeur initiale 1
            pages_start.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        }
        if (pages_end != null) {
            // Utilisation de la valeur initiale 1
            pages_end.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        }
        if (volume != null) {
            volume.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        }
        if (nb_pages != null) {
            nb_pages.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 10));
        }

        // --- Configuration des ChoiceBoxes ---
        if (langue != null) {
            langue.getItems().addAll("FR", "Anglais", "Arabe");
        }
        if (catg != null) {
            // CORRECTION: Ajout direct des valeurs d'énumération converties en String
            catg.getItems().addAll(Categorie.values());

        }
    }

    // --- setDocument (Initialisation) ---
    @Override
    public void setDocument(Document document) {
        if (document instanceof ArticleUniversitaire) {
            this.document = document;
            initializeFields((ArticleUniversitaire) document);
        } else {
            System.err.println("Erreur: Le document n'est pas un ArticleUniversitaire.");
        }
    }

    @Override
    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    private void initializeFields(ArticleUniversitaire article) {
        // Champs communs (Basés sur la classe Document)
        if (titre != null)
            titre.setText(article.getTitre());
        if (Auteurs != null)
            Auteurs.setText(String.join(", ", article.getAuteurs()));
        if (editeur != null)
            editeur.setText(article.getEditeur());
        if (resume != null)
            resume.setText(article.getResume());
        if (mot_cles != null)
            mot_cles.setText(String.join(", ", article.getMotsCles()));

        // Initialisation des valeurs (Nécessite la configuration des ChoiceBox/Spinner)
        if (annee_p != null && annee_p.getValueFactory() != null) {
            annee_p.getValueFactory().setValue(article.getAnneePublication());
        }
        if (langue != null && langue.getSelectionModel() != null) {
            langue.getSelectionModel().select(article.getLangue());
        }
        if (catg != null && catg.getSelectionModel() != null) {
            catg.getSelectionModel().select(article.getCategorie());
        }

        // Champs spécifiques à ArticleUniversitaire
        if (titre_revue != null)
            titre_revue.setText(article.getTitreRevue());

        // DOI (TextField)
        if (doi != null && article.getDOI() != null) {
            doi.setText(article.getDOI());
        }

        // VOLUME (Spinner)
        if (volume != null && volume.getValueFactory() != null) {
            volume.getValueFactory().setValue(article.getVolume());
        }
        if (nb_pages != null && nb_pages.getValueFactory() != null) {
            nb_pages.getValueFactory().setValue(article.getNumero());

        }
        try {
            String s = article.getPages();
            String sp = trouverCaractereNonChiffre(s);

            // 1. Vérifiez que le format est valide (contient un tiret et n'est pas vide)
            if (s != null && s.contains(sp)) {
                String[] pagesParts = s.split(sp);
                if (pagesParts.length == 2) {
                    // Les lignes ci-dessous sont les lignes où l'erreur se produit
                    int a = Integer.parseInt(pagesParts[0].trim()); // .trim() pour enlever les espaces
                    int b = Integer.parseInt(pagesParts[1].trim());

                    if (pages_start != null && pages_start.getValueFactory() != null) {
                        pages_start.getValueFactory().setValue(a);
                    }
                    if (pages_end != null && pages_end.getValueFactory() != null) {
                        pages_end.getValueFactory().setValue(b);
                    }
                }
            }

            // Le Spinner nb_pages est mis à jour avec la propriété 'numero' de l'article
            // (comme dans votre code précédent)
            if (nb_pages != null && nb_pages.getValueFactory() != null) {
                nb_pages.getValueFactory().setValue(article.getNumero());
            }

        } catch (NumberFormatException e) {
            // Afficher une alerte ou un message d'erreur si les données de la BDD sont
            // mauvaises
            System.err.println("Erreur de format de pages dans les données du document : " + e.getMessage());
            showAlert("Erreur de Données",
                    "Le format des pages ('50?78') est invalide. Veuillez le corriger dans la base de données (utiliser le format 'Début-Fin').",
                    Alert.AlertType.ERROR);
        }

        // Retrait de la ligne redondante: if (pages_start != null)
        // pages.setText(article.getPages());
    }

    public String trouverCaractereNonChiffre(String chaine) {
        if (chaine == null || chaine.isEmpty()) {
            return null; // Retourne null pour une chaîne invalide ou vide
        }

        for (int i = 0; i < chaine.length(); i++) {
            char caractere = chaine.charAt(i);

            if (!Character.isDigit(caractere)) {
                // Convertit le char trouvé en String avant de le retourner
                return String.valueOf(caractere);
            }
        }
        // Retourne null si aucun non-chiffre n'est trouvé
        return null;
    }

    // --- Logique des boutons ---

    @FXML
    void anuller(ActionEvent event) {
        Stage stage = (Stage) Anuller.getScene().getWindow();
        stage.close();
    }

    // Dans DetailsArticleUnivController.java
    // ... (code précédent)

    @FXML
    void update(ActionEvent event) {
        if (documentManager == null || !(document instanceof ArticleUniversitaire)) {
            showAlert("Erreur", "Problème d'initialisation du manager ou du document.", Alert.AlertType.ERROR);
            return;
        }

        ArticleUniversitaire article = (ArticleUniversitaire) this.document;

        // --- 1. Préparation des valeurs des Spinners ---

        // Récupération sécurisée des pages
        final Integer debutPage = pages_start != null ? pages_start.getValue() : null;
        final Integer finPage = pages_end != null ? pages_end.getValue() : null;

        if (debutPage == null || finPage == null) {
            showAlert("Erreur de donnée",
                    "Les numéros de page de début et de fin doivent être des nombres entiers valides.",
                    Alert.AlertType.ERROR);
            return;
        }
        if (debutPage > finPage) {
            showAlert("Erreur de logique", "La page de début ne peut pas être supérieure à la page de fin.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Reconstruire la chaîne de pages (ex: "201-225")
        final String nouvellesPages = debutPage + "-" + finPage;

        // Récupération de la catégorie sélectionnée (ChoiceBox<Categorie>)
        final Categorie nouvelleCategorie = catg.getValue();
        if (nouvelleCategorie == null) {
            showAlert("Erreur de donnée", "Veuillez sélectionner une catégorie.", Alert.AlertType.ERROR);
            return;
        }

        // Assurez-vous que les Spinners sans valeur n'arrêtent pas l'update
        final Integer nouveauVolume = volume != null ? volume.getValue() : 1; // Utiliser une valeur par défaut
        final Integer nouvelleAnnee = annee_p != null ? annee_p.getValue() : 2025; // Utiliser une valeur par défaut

        // Récupération du numéro de page
        final Integer nouveauNbPages = nb_pages != null ? nb_pages.getValue() : 1;

        try {
            // 2. Appeler le manager pour appliquer toutes les modifications via le Consumer
            boolean success = documentManager.modifierDocument(article.getIdDocument(), doc -> {

                if (doc instanceof ArticleUniversitaire) {
                    ArticleUniversitaire articleModifie = (ArticleUniversitaire) doc;

                    // 1. Mise à jour des champs communs

                    // TextFields simples
                    if (titre != null)
                        articleModifie.setTitre(titre.getText());
                    if (editeur != null)
                        articleModifie.setEditeur(editeur.getText());
                    if (resume != null)
                        articleModifie.setResume(resume.getText());

                    // Spinners
                    articleModifie.setAnneePublication(nouvelleAnnee);
                    articleModifie.setVolume(nouveauVolume);

                    // ChoiceBoxes/Enums
                    articleModifie.setCategorie(nouvelleCategorie);
                    if (langue != null)
                        articleModifie.setLangue(langue.getValue());

                    // Listes (Auteurs et Mots-clés)
                    // ********* ATTENTION: REQUIERT une implémentation de split() *********
                    if (Auteurs != null) {
                        // Supposons que les auteurs sont séparés par des virgules
                        List<String> nouveauxAuteurs = Arrays.asList(Auteurs.getText().split("\\s*,\\s*"));
                        articleModifie.setAuteurs(nouveauxAuteurs);
                    }
                    if (mot_cles != null) {
                        // Supposons que les mots-clés sont séparés par des virgules
                        List<String> nouveauxMotsCles = Arrays.asList(mot_cles.getText().split("\\s*,\\s*"));
                        articleModifie.setMotsCles(nouveauxMotsCles);
                    }

                    // 2. Mise à jour des champs spécifiques
                    if (titre_revue != null)
                        articleModifie.setTitreRevue(titre_revue.getText());
                    if (doi != null)
                        articleModifie.setDOI(doi.getText());

                    // 3. Mise à jour des pages et autres numéros

                    // Pages: Utilisation de la chaîne reconstruite "début-fin"
                    articleModifie.setPages(nouvellesPages);

                    // ATTENTION: La propriété 'numero' de ArticleUniversitaire correspond-elle à
                    // 'nb_pages' ?
                    // Si ArticleUniversitaire.numero = le numéro de la revue, alors il faut le
                    // mettre à jour.
                    // Si 'nb_pages' est le nombre total de pages, il doit être calculé ou géré
                    // autrement.
                    // Je suppose que vous vouliez mettre à jour la propriété 'numero' de l'article
                    // avec le contenu du Spinner 'nb_pages'.
                    articleModifie.setNumero(nouveauNbPages);
                }
            });

            if (success) {
                parent.refreshTable();
                showAlert("Succès", "Article mis à jour avec succès.", Alert.AlertType.INFORMATION);
                anuller(null);
            } else {
                showAlert("Erreur", "Échec de la mise à jour : Document non trouvé ou modification annulée.",
                        Alert.AlertType.ERROR);
            }

        } catch (DocumentInexistantException e) {
            showAlert("Erreur", "Le document à modifier n'existe plus.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur BDD", "Une erreur est survenue lors de la sauvegarde : " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    DocumentsController parent;

    @Override
    public void setParent(DocumentsController table) {
        parent = table;
    }
}