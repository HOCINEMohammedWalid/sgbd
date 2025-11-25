package com.sgeb.sgbd.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.sgeb.sgbd.model.DocumentManager;

public class AjouterDocumentController {

    @FXML
    void ajouterDVD(ActionEvent event) {
        ouvrirFenetre("/com/sgeb/sgbd/view/AjouteDVD.fxml", "Ajouter DVD");
    }

    @FXML
    void ajouterEbook(ActionEvent event) {
        ouvrirFenetre("/com/sgeb/sgbd/view/AjouteEbook.fxml", "Ajouter Ebook");
    }

    @FXML
    void ajouterLivre(ActionEvent event) {
        ouvrirFenetre("/com/sgeb/sgbd/view/AjouteLivre.fxml", "Ajouter Livre");
    }

    @FXML
    void ajouterMagazine(ActionEvent event) {
        ouvrirFenetre("/com/sgeb/sgbd/view/AjouteMagazine.fxml", "Ajouter Magazine");
    }

    @FXML
    void ajouterThese(ActionEvent event) {
        ouvrirFenetre("/com/sgeb/sgbd/view/AjouteThese.fxml", "Ajouter Thèse");
    }

    @FXML
    void ajouter_ArticleUniv(ActionEvent event) {
        ouvrirFenetre("/com/sgeb/sgbd/view/AjouteArticleUniv.fxml", "Ajouter Article Universitaire");
    }

    private DocumentManager documentManager;
    private DocumentsController parentController;

    public void setManager(DocumentManager manager) {
        this.documentManager = manager;
    }

    public void setParentController(DocumentsController controller) {
        this.parentController = controller;
    }

    private void ouvrirFenetre(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            // Injection du DocumentManager
            try {
                java.lang.reflect.Method m = controller.getClass().getMethod("setManager", DocumentManager.class);
                m.invoke(controller, documentManager);
            } catch (NoSuchMethodException e) {
                // Le controller n'a pas setManager → normal
            } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                e.printStackTrace();
            }

            // Injection du parent AjouterDocumentController
            try {
                java.lang.reflect.Method m = controller.getClass().getMethod("setParent",
                        DocumentsController.class);
                m.invoke(controller, parentController);
            } catch (NoSuchMethodException e) {
                // Le controller n'a pas setParent → normal
            } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                e.printStackTrace();
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(titre);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
