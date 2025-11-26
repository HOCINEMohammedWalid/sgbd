package com.sgeb.sgbd.model;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class Emprunt {

    private int idEmprunt;
    private final Document document;
    private final Adherent adherent;
    private final LocalDate dateEmprunt;
    private final LocalDate dateRetourPrevue;
    private LocalDate dateRetourReelle;
    private double penalite;
    private boolean payee;

    public Emprunt(int idEmprunt, Document document, Adherent adherent, LocalDate dateEmprunt,
            LocalDate dateRetourPrevue) {
        this.idEmprunt = idEmprunt;
        this.document = document;
        this.adherent = adherent;
        this.dateEmprunt = dateEmprunt;
        this.dateRetourPrevue = dateRetourPrevue;
        this.penalite = 0;
    }

    public Emprunt(int idEmprunt, Document document, Adherent adherent, LocalDate dateEmprunt,
            LocalDate dateRetourPrevue, LocalDate dateRetourReelle, double penalite, boolean payee) {

        this.idEmprunt = idEmprunt;
        this.document = document;
        this.adherent = adherent;
        this.dateEmprunt = dateEmprunt;
        this.dateRetourPrevue = dateRetourPrevue;
        this.dateRetourReelle = dateRetourReelle;
        this.penalite = penalite;
        this.payee = payee;
    }

    public int getIdEmprunt() {
        return idEmprunt;
    }

    public Document getDocument() {
        return document;
    }

    public Adherent getAdherent() {
        return adherent;
    }

    public LocalDate getDateEmprunt() {
        return dateEmprunt;
    }

    public LocalDate getDateRetourPrevue() {
        return dateRetourPrevue;
    }

    public LocalDate getDateRetourReelle() {
        return dateRetourReelle;
    }

    public double getPenalite() {
        return penalite;
    }

    public boolean isPayee() {
        return payee;
    }

    public void setDateRetourReelle(LocalDate dateRetourReelle) {
        this.dateRetourReelle = dateRetourReelle;
    }

    public void setPenalite(double penalite) {
        this.penalite = penalite;
    }

    public void setIdEmprunt(int idEmprunt) {
        this.idEmprunt = idEmprunt;
    }

    public void marquerPenalitePayee() {
        this.payee = true;
    }

    public String getDocumentTitre() {
        if (this.document != null) {

            return document.getTitre();
        } else {
            return "[document non lié]";
        }

    }

    // Dans com.sgeb.sgbd.model.Emprunt (Ligne 93 ou aux alentours)
    public String getNomCompletAdherent() {

        if (this.adherent != null) {

            String nom = (this.adherent.getNom() != null) ? this.adherent.getNom() : "";
            String prenom = (this.adherent.getPrenom() != null) ? this.adherent.getPrenom() : "";

            return nom + " " + prenom;
        } else {
            return "[Adhérent non lié]";
        }
    }

}
