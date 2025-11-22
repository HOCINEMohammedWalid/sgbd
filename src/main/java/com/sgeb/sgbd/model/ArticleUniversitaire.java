package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.enums.TypeDocument;

public class ArticleUniversitaire extends Document {
    private String titreRevue;
    private int volume;
    private int numero;
    private String pages;
    private String DOI;

    public ArticleUniversitaire(int idDocument, String titre, String titreRevue, int volume, int numero, String pages,
            String DOI) {
        super(idDocument, titre);
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

    @Override
    public String toString() {
        return getDescriptionComplete();
    }
}
