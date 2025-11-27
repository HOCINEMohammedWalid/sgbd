
package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Adherent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;

public class PagePrincipaleNonAdminController implements ManagerLoader {

    // --- Managers reçus depuis le Main ou AppLoader ---
    private DocumentManager documentManager;
    private AdherentManager adherentManager;
    private EmpruntManager empruntManager;
    private Adherent adherentConnecte;
    private String motDePasseHacheConnecte;

    public void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM) {
        this.documentManager = docM;
        this.adherentManager = adhM;
        this.empruntManager = empM;
    }

    public void setAdherentConnecte(Adherent adherent) {
        this.adherentConnecte = adherent;
    }

    public void setMotDePasseHacheConnecte(String hash) {
        this.motDePasseHacheConnecte = hash;
    }

    @FXML
    private StackPane ContentArea;

    // Interface pour initialiser les controllers dynamiquement
    private interface ControllerInitializer {
        void init(Object controller);
    }

    // ---------- MÉTHODE GÉNÉRIQUE D'AFFICHAGE (Copie de la version Admin)
    // ----------
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

    @FXML
    void Documents(ActionEvent event) {
        // CHARGEMENT DE LA VUE NON-ADMIN
        loadPage("/com/sgeb/sgbd/view/TableauDocumentNonAdmin.fxml", controller -> {
            // CORRECTION: Conversion vers le contrôleur Non-Admin
            DocumentsControllerNonAdmin c = (DocumentsControllerNonAdmin) controller;
            c.setManagers(documentManager, adherentManager, empruntManager);// Il est plus simple d'utiliser le
                                                                            // setManagers qui prend un seul argument
            c.setAdherent(adherentConnecte); // si les autres managers ne sont pas nécessaires.
        });
    }

    @FXML
    void mes_emprints(ActionEvent event) {

        loadPage("/com/sgeb/sgbd/view/TableauMesEmpunts.fxml", controller -> {

            MesEmpruntsController c = (MesEmpruntsController) controller;

            c.setDependencies(empruntManager, adherentConnecte);

        });
    }

    @FXML
    void profil(ActionEvent event) {
        // Logique de chargement du profil Non-Admin
        loadPage("/com/sgeb/sgbd/view/ProfilAdherent.fxml", controller -> {
            ProfilAdherentController profilController = (ProfilAdherentController) controller;

            profilController.setManagers(documentManager, adherentManager, empruntManager);
            profilController.setAdherent(adherentConnecte);
            // avoir adherentConnecte et motDePasseHacheConnecte définis dans cette classe
            if (adherentConnecte != null && motDePasseHacheConnecte != null) {

                profilController.initData(adherentManager, adherentConnecte, motDePasseHacheConnecte);
            } else {
                System.err.println("Erreur: L'adhérent ou le mot de passe connecté est manquant.");
            }
        });
    }
}
