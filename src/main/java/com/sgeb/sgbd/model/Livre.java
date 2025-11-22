package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.enums.TypeDocument;
import com.sgeb.sgbd.model.enums.Categorie;
import java.util.List;

public class Livre extends Document {

    private String ISBN;
    private int nbPages;
    private String collection;

    // Constructeur complet
    public Livre(int idDocument, String titre, List<String> auteurs, int anneePublication,
            String editeur, String resume, Categorie categorie, List<String> motsCles,
            String langue, String isbn, int nbPages, String collection) {
        super(idDocument, titre, auteurs, anneePublication, editeur, resume, categorie, motsCles, langue,
                TypeDocument.LIVRE);
        this.ISBN = isbn;
        this.nbPages = nbPages;
        this.collection = collection;
    }

    // Getters et setters spécifiques
    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String isbn) {
        this.ISBN = isbn;
    }

    public int getNbPages() {
        return nbPages;
    }

    public void setNbPages(int nbPages) {
        this.nbPages = nbPages;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    // Méthodes abstraites implémentées
    @Override
    public String getDescriptionComplete() {
        return String.format(
                "Livre: %s\nAuteurs: %s\nAnnée: %d\nÉditeur: %s\nCollection: %s\nPages: %d\nISBN: %s\nLangue: %s\nCatégorie: %s\nRésumé: %s\nMots-clés: %s",
                titre,
                String.join(", ", auteurs),
                anneePublication,
                editeur,
                collection != null ? collection : "N/A",
                nbPages,
                ISBN != null ? ISBN : "N/A",
                langue,
                categorie != null ? categorie : "N/A",
                resume != null ? resume : "N/A",
                String.join(", ", motsCles));
    }

    @Override
    public void afficherResume() {
        System.out.println(
                "Résumé du livre \"" + titre + "\": " + (resume != null ? resume : "Aucun résumé disponible."));
    }

    @Override
    public String toString() {
        return super.toString() + " - Livre ISBN:" + ISBN;
    }

}
