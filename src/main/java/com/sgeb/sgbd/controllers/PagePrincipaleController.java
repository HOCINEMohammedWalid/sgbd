package com.sgeb.sgbd.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;

public class PagePrincipaleController {

    // --- Managers reçus depuis le Main ou AppLoader ---
    private DocumentManager documentManager;
    private AdherentManager adherentManager;
    private EmpruntManager empruntManager;

    public void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM) {
        this.documentManager = docM;
        this.adherentManager = adhM;
        this.empruntManager = empM;
    }

    // Zone centrale
    @FXML
    private StackPane ContentArea;

    // ---------- MÉTHODE GÉNÉRIQUE D'AFFICHAGE ----------
    private void loadPage(String fxmlPath, ControllerInitializer initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            if (loader.getLocation() == null) {
                throw new RuntimeException("FXML introuvable : " + fxmlPath);
            }

            Parent page = loader.load();

            if (initializer != null) {
                initializer.init(loader.getController());
            }

            ContentArea.getChildren().clear();
            ContentArea.getChildren().add(page);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement du FXML : " + fxmlPath, e);
        }
    }

    // Interface pour initialiser les controllers dynamiquement
    private interface ControllerInitializer {
        void init(Object controller);
    }

    // =========================================================
    // NAVIGATION BOUTONS
    // =========================================================

    @FXML

    void Documents(ActionEvent event) {
        loadPage("/com/sgeb/sgbd/view/TableauDocumentAdmin.fxml", controller -> {
            DocumentsController c = (DocumentsController) controller;
            c.setManagers(documentManager, adherentManager, empruntManager);
        });
    }

    @FXML
    void adherents(ActionEvent event) {
        /*
         * loadPage("/view/Adherents.fxml", controller -> {
         * AdherentsController c = (AdherentsController) controller;
         * c.setManagers(adherentManager, empruntManager);
         * });
         */
    }

    @FXML
    void emprunts(ActionEvent event) {
        /*
         * loadPage("/view/Emprunts.fxml", controller -> {
         * EmpruntsController c = (EmpruntsController) controller;
         * c.setManagers(empruntManager, adherentManager, documentManager);
         * });
         */
    }

    @FXML
    void profil(ActionEvent event) {
        /*
         * loadPage("/view/Profil.fxml", controller -> {
         * ProfilController c = (ProfilController) controller;
         * c.setManagers(documentManager, adherentManager, empruntManager);
         * });
         */
    }
}
