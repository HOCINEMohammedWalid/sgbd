
package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.enums.TypeDocument;

public class DVD extends Document {
    private String realisateur;
    private int duree; // en minutes
    private String classification;

    public DVD(int idDocument, String titre, String realisateur, int duree, String classification) {
        super(idDocument, titre);
        this.realisateur = realisateur;
        this.duree = duree;
        this.classification = classification;
        this.typeDocument = TypeDocument.DVD;
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

    @Override
    public String toString() {
        return getDescriptionComplete();
    }
}
