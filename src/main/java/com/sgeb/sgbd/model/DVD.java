
package com.sgeb.sgbd.model;

import java.util.List;

import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.enums.TypeDocument;

public class DVD extends Document {
    private String realisateur;
    private int duree; // en minutes
    private String classification;

    public DVD(int idDocument, String titre, List<String> auteurs,
            int anneePublication,
            String editeur,
            String resume,
            Categorie categorie,
            List<String> motsCles,
            String langue, String realisateur, int duree, String classification) {

        super(idDocument, titre, auteurs,
                anneePublication,
                editeur,
                resume,
                categorie,
                motsCles,
                langue, TypeDocument.DVD);

        this.realisateur = realisateur;
        this.duree = duree;
        this.classification = classification;

    }

    // Getters et setters
    public String getRealisateur() {
        return realisateur;
    }

    public void setRealisateur(String realisateur) {
        this.realisateur = realisateur;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    // Méthodes abstraites de Document
    @Override
    public String getDescriptionComplete() {
        return String.format("DVD[id=%d, titre='%s', realisateur='%s', duree=%d min, classification='%s']",
                idDocument, titre, realisateur, duree, classification);
    }

    @Override
    public void afficherResume() {
        System.out.println("Résumé du DVD : " + titre);
    }

    // Dans com.sgeb.sgbd.model.DVD

    @Override
    public String toString() {

        // Convertir la durée en minutes en format Hh MMmin (par exemple, 95min -> 1h
        // 35min)
        String dureeFormatee;
        if (duree > 0) {
            long heures = duree / 60;
            long minutes = duree % 60;
            if (heures > 0) {
                dureeFormatee = String.format("%dh %dmin", heures, minutes);
            } else {
                dureeFormatee = String.format("%dmin", minutes);
            }
        } else {
            dureeFormatee = "Durée inconnue";
        }

        // Formatage complet des informations du DVD
        return String.format(
                "DVD [ID: %d] : %s\n" +
                        "  Réalisateur: %s | Année: %d\n" +
                        "  Durée: %s | Classification: %s\n" +
                        "  Catégorie: %s | Langue: %s",
                idDocument,
                titre,
                (realisateur != null && !realisateur.isEmpty() ? realisateur : "N/A"),
                anneePublication,
                dureeFormatee,
                (classification != null && !classification.isEmpty() ? classification : "N/A"),
                (categorie != null ? categorie.name() : "N/A"),
                (langue != null && !langue.isEmpty() ? langue : "N/A"));
    }
}
