package com.sgeb.sgbd.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Charge la vue d'accueil par défaut au démarrage
        loadContent("/com/sgeb/sgbd/view/admin/dashboard-home-view.fxml");
    }

    @FXML
    private void handleDashboardClick(ActionEvent event) {
        loadContent("/com/sgeb/sgbd/view/admin/dashboard-home-view.fxml");
    }
    /*
     * @FXML
     * private void handleViewMembersClick(ActionEvent event) {
     * loadContent("/com/sgeb/sgbd/view/admin/user-management-view.fxml");
     * }
     * 
     * @FXML
     * private void handleViewBorrowingsClick(ActionEvent event) {
     * loadContent("/com/sgeb/sgbd/view/admin/borrowing-view.fxml");
     * }
     * 
     * @FXML
     * private void handleViewDocumentsClick(ActionEvent event) {
     * loadContent("/com/sgeb/sgbd/view/admin/document-management-view.fxml");
     * }
     * 
     * @FXML
     * private void handleChangePasswordClick(ActionEvent event) {
     * loadContent("/com/sgeb/sgbd/view/admin/password-change-view.fxml");
     * }
     * 
     * @FXML
     * private void handleLogout(ActionEvent event) {
     * // Redirection vers l'écran de connexion/accueil
     * try {
     * Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
     * FXMLLoader loader = new
     * FXMLLoader(getClass().getResource("/com/sgeb/sgbd/view/admin-auth-view.fxml")
     * );
     * Parent root = loader.load();
     * stage.getScene().setRoot(root);
     * } catch (Exception e) {
     * e.printStackTrace();
     * System.err.println("Failed to load Logout view.");
     * }
     * }
     */

    // Méthode générique pour charger une sous-vue dans la zone centrale
    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            contentArea.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la sous-vue FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }
}