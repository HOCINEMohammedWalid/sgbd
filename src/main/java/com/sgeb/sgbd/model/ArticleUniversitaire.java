package com.sgeb.sgbd.model;

import java.util.List;

import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.enums.TypeDocument;

public class ArticleUniversitaire extends Document {
    private String titreRevue;
    private int volume;
    private int numero;
    private String pages;
    private String DOI;

    public ArticleUniversitaire(int idDocument, String titre, List<String> auteurs,
            int anneePublication,
            String editeur,
            String resume,
            Categorie categorie,
            List<String> motsCles,
            String langue, String titreRevue, int volume, int numero, String pages,
            String DOI) {
        super(idDocument, titre, auteurs,
                anneePublication,
                editeur,
                resume,
                categorie,
                motsCles,
                langue,
                TypeDocument.ARTICLE);
        this.titreRevue = titreRevue;
        this.volume = volume;
        this.numero = numero;
        this.pages = pages;
        this.DOI = DOI;
        this.typeDocument = TypeDocument.ARTICLE;
    }

    // Getters et setters
    public String getTitreRevue() {
        return titreRevue;
    }

    public void setTitreRevue(String titreRevue) {
        this.titreRevue = titreRevue;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getDOI() {
        return DOI;
    }

    public void setDOI(String DOI) {
        this.DOI = DOI;
    }

    // Méthodes abstraites de Document
    @Override
    public String getDescriptionComplete() {
        return String.format("Article[id=%d, titre='%s', revue='%s', volume=%d, numero=%d, pages='%s', DOI='%s']",
                idDocument, titre, titreRevue, volume, numero, pages, DOI);
    }

    @Override
    public void afficherResume() {
        System.out.println("Résumé de l'article : " + super.resume);
    }

    // Dans com.sgeb.sgbd.model.ArticleUniversitaire

    @Override
    public String toString() {

        // 1. Formate la liste des auteurs (similaire à la classe Document mais
        // réimplémenté ici pour être auto-suffisant)
        String auteursStr;
        if (auteurs.isEmpty()) {
            auteursStr = "N/A";
        } else if (auteurs.size() == 1) {
            auteursStr = auteurs.get(0);
        } else {
            // Liste tous les auteurs séparés par des virgules
            auteursStr = String.join(", ", auteurs);
        }

        // 2. Formatage complet des informations de l'article
        String doiInfo = (DOI != null && !DOI.isEmpty()) ? String.format(" | DOI: %s", DOI) : "";

        return String.format(
                "Article Universitaire [ID: %d]\n" +
                        "  Titre: %s\n" +
                        "  Auteurs: %s\n" +
                        "  Revue: %s, Volume %d, Numéro %d, Pages %s\n" +
                        "  Publication: %s (%d) | Éditeur: %s%s",
                idDocument,
                titre,
                auteursStr,
                (titreRevue != null ? titreRevue : "N/A"),
                volume,
                numero,
                pages,
                (categorie != null ? categorie.name() : "N/A"),
                anneePublication,
                (editeur != null && !editeur.isEmpty() ? editeur : "Inconnu"),
                doiInfo);
    }

}
