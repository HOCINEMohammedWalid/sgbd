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

        // 1. Gère et formate la liste des auteurs (copié/adapté de la classe Document
        // pour l'autonomie)
        String auteursStr;
        if (auteurs.isEmpty()) {
            auteursStr = "Auteur(s) non spécifié(s)";
        } else if (auteurs.size() == 1) {
            auteursStr = auteurs.get(0);
        } else {
            // Liste les deux premiers auteurs, puis ajoute "et al." si plus de deux
            StringBuilder sb = new StringBuilder();
            sb.append(auteurs.get(0));
            if (auteurs.size() > 1) {
                sb.append(", ").append(auteurs.get(1));
            }
            if (auteurs.size() > 2) {
                sb.append(", et al.");
            }
            auteursStr = sb.toString();
        }

        // 2. Formatage complet des informations du Livre
        return String.format(
                "Livre [ID: %d] : %s\n" +
                        "  Auteur(s): %s\n" +
                        "  Publié en %d par %s (Collection: %s)\n" +
                        "  ISBN: %s | Pages: %d | Catégorie: %s",
                idDocument,
                titre,
                auteursStr,
                anneePublication,
                (editeur != null && !editeur.isEmpty() ? editeur : "Inconnu"),
                (collection != null && !collection.isEmpty() ? collection : "N/A"),
                (ISBN != null && !ISBN.isEmpty() ? ISBN : "N/A"),
                nbPages,
                (categorie != null ? categorie.name() : "N/A"));
    }

}
