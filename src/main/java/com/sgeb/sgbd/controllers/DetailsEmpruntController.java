package com.sgeb.sgbd.controllers;

import com.sgeb.sgbd.model.Emprunt;
import com.sgeb.sgbd.model.EmpruntManager; // Nécessaire si les actions modifient la BDD
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DetailsEmpruntController {

    private Emprunt emprunt;
    // Le manager est nécessaire pour persister les changements (Retour, Paiement)
    private EmpruntManager empruntManager;
    private EmpruntsController parentController; // Pour rafraîchir la table principale

    @FXML
    private Button Anuller; // Mieux nommé: btnAnnuler

    @FXML
    private Button Retour; // Mieux nommé: btnRetour

    @FXML
    private TextArea info; // Mieux nommé: taInfo

    @FXML
    private TextField nom; // Mieux nommé: tfNomAdherent

    @FXML
    private Button payment; // Mieux nommé: btnPayment

    @FXML
    private TextField titre; // Mieux nommé: tfTitreDocument

    /**
     * Méthode d'initialisation pour recevoir l'objet Emprunt et les dépendances.
     */
    public void setEmprunt(Emprunt emprunt, EmpruntManager empruntManager, EmpruntsController parentController) {
        this.emprunt = emprunt;
        this.empruntManager = empruntManager;
        this.parentController = parentController;
        displayEmpruntDetails();
        checkEmpruntStatus();
    }

    /**
     * Affiche les détails de l'emprunt dans les champs.
     */
    private void displayEmpruntDetails() {
        if (emprunt != null) {
            // Utilise les getters sécurisés de la classe Emprunt
            nom.setText(emprunt.getNomCompletAdherent());
            titre.setText(emprunt.getDocumentTitre());

            // --- DÉBUT DE LA LOGIQUE DE RETARD CORRIGÉE ---
            long joursRetard;
            LocalDate datePrevue = emprunt.getDateRetourPrevue();
            LocalDate dateReelle = emprunt.getDateRetourReelle();

            if (dateReelle == null) {
                // Cas 1 : Emprunt non rendu. Le retard est calculé jusqu'à AUJOURD'HUI.
                if (datePrevue != null && datePrevue.isBefore(LocalDate.now())) {
                    joursRetard = ChronoUnit.DAYS.between(datePrevue, LocalDate.now());
                } else {
                    joursRetard = 0L;
                }
            } else {
                // Cas 2 : Emprunt rendu (retard fixe). Calculer le retard basé sur la date
                // réelle.
                if (datePrevue != null && dateReelle.isAfter(datePrevue)) {
                    joursRetard = ChronoUnit.DAYS.between(datePrevue, dateReelle);
                } else {
                    joursRetard = 0L;
                }
            }
            // --- FIN DE LA LOGIQUE DE RETARD CORRIGÉE ---

            double penalite = emprunt.getPenalite(); // Cette valeur est supposée être calculée dans la classe Emprunt

            // La variable joursRetard est maintenant correcte, elle peut être utilisée pour
            // le statut et l'affichage.
            String statutRetard = (joursRetard > 0) ? "OUI" : "NON";

            String details = String.format(
                    "ID Emprunt: %d\n" +
                            "Date Emprunt: %s\n" +
                            "Retour Prévu: %s\n" +
                            "Retour Réel: %s\n" +
                            "------------------------------------\n" +
                            "Statut:\n" +
                            "  - En Retard: %s\n" +
                            "  - Jours de Retard: %d\n" +
                            "  - Pénalité: %.2f € (Payée: %s)",
                    emprunt.getIdEmprunt(),
                    emprunt.getDateEmprunt(),
                    emprunt.getDateRetourPrevue(),
                    (dateReelle != null ? dateReelle.toString() : "Non rendu"), // Utiliser la variable locale
                    statutRetard, // Utiliser la variable de statut
                    joursRetard, // Utiliser le retard corrigé
                    penalite,
                    (emprunt.isPayee() ? "OUI" : "NON"));

            info.setText(details);
        }
    }

    /**
     * Active/Désactive les boutons en fonction du statut de l'emprunt.
     */
    private void checkEmpruntStatus() {
        if (emprunt != null) {
            boolean estRendu = emprunt.getDateRetourReelle() != null;
            boolean aPayer = emprunt.getPenalite() > 0 && !emprunt.isPayee();

            Retour.setDisable(estRendu);
            payment.setDisable(estRendu && !aPayer);
        }
    }

    // =============================================
    // ACTIONS DES BOUTONS
    // =============================================

    @FXML
    void anuller(ActionEvent event) {
        // Ferme la fenêtre actuelle
        Stage stage = (Stage) Anuller.getScene().getWindow();
        stage.close();
    }

    @FXML
    void payment(ActionEvent event) {
        if (emprunt == null || emprunt.isPayee())
            return;

        try {

            empruntManager.payerPenalite(emprunt);

            showAlert("Succès", "Paiement de la pénalité de " + emprunt.getPenalite() + " € enregistré.",
                    Alert.AlertType.INFORMATION);

            displayEmpruntDetails();
            checkEmpruntStatus();
            if (parentController != null) {
                parentController.refreshTable(); // Rafraîchit la table principale
            }

        } catch (Exception e) {
            showAlert("Erreur BDD", "Erreur lors de l'enregistrement du paiement : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    void retour(ActionEvent event) {
        if (emprunt == null || emprunt.getDateRetourReelle() != null)
            return;

        try {

            empruntManager.retour(emprunt);

            showAlert("Succès", "Le document a été marqué comme rendu aujourd'hui.", Alert.AlertType.INFORMATION);

            displayEmpruntDetails();
            checkEmpruntStatus();
            if (parentController != null) {
                parentController.refreshTable();
            }

        } catch (Exception e) {
            showAlert("Erreur BDD", "Erreur lors de l'enregistrement du retour : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
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