package com.sgeb.sgbd.model;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class Emprunt {
    private static final AtomicInteger compteur = new AtomicInteger(0);

    private final int idEmprunt;
    private final Document document;
    private final Adherent adherent;
    private final LocalDate dateEmprunt;
    private final LocalDate dateRetourPrevue;
    private LocalDate dateRetourReelle;
    private double penalite;

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
            LocalDate dateRetourPrevue, LocalDate dateRetourReelle, double penalite) {

        this.idEmprunt = idEmprunt;
        this.document = document;
        this.adherent = adherent;
        this.dateEmprunt = dateEmprunt;
        this.dateRetourPrevue = dateRetourPrevue;
        this.dateRetourReelle = dateRetourReelle;
        this.penalite = penalite;
    }

    public int getIdEmprunt() {
        return idEmprunt;
    }

    public Document getExemplaire() {
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

    public void setDateRetourReelle(LocalDate dateRetourReelle) {
        this.dateRetourReelle = dateRetourReelle;
    }

    public void setPenalite(double penalite) {
        this.penalite = penalite;
    }

    @Override
    public String toString() {
        return String.format(
                "Emprunt[%d] Document='%s', Adhérent='%s', Emprunt=%s, Retour prévu=%s, Retour réel=%s, Pénalité=%.2f€",
                idEmprunt,
                document.getTitre(),
                adherent.getNomComplet(),
                dateEmprunt,
                dateRetourPrevue,
                dateRetourReelle != null ? dateRetourReelle : "non retourné",
                penalite);
    }
}
