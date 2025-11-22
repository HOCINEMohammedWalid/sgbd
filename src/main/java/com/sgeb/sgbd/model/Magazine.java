package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.enums.TypeDocument;
import com.sgeb.sgbd.model.enums.Categorie;
import java.time.LocalDate;
import java.util.List;

public class Magazine extends Document {

    private int numero;
    private String periodicite;
    private LocalDate datePublication;

    // Constructeur complet
    public Magazine(int idDocument, String titre, List<String> auteurs, int anneePublication,
            String editeur, String resume, Categorie categorie, List<String> motsCles,
            String langue, int numero, String periodicite, LocalDate datePublication) {
        super(idDocument, titre, auteurs, anneePublication, editeur, resume, categorie, motsCles, langue,
                TypeDocument.MAGAZINE);
        this.numero = numero;
        this.periodicite = periodicite;
        this.datePublication = datePublication;
    }

    // Getters et setters spécifiques
    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getPeriodicite() {
        return periodicite;
    }

    public void setPeriodicite(String periodicite) {
        this.periodicite = periodicite;
    }

    public LocalDate getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDate datePublication) {
        this.datePublication = datePublication;
    }

    // Méthodes abstraites implémentées
    @Override
    public String getDescriptionComplete() {
        return String.format(
                "Magazine: %s\nAuteurs: %s\nAnnée: %d\nÉditeur: %s\nNuméro: %d\nPériodicité: %s\nDate de publication: %s\nLangue: %s\nCatégorie: %s\nRésumé: %s\nMots-clés: %s",
                titre,
                auteurs != null ? String.join(", ", auteurs) : "N/A",
                anneePublication,
                editeur,
                numero,
                periodicite != null ? periodicite : "N/A",
                datePublication != null ? datePublication.toString() : "N/A",
                langue,
                categorie != null ? categorie : "N/A",
                resume != null ? resume : "N/A",
                motsCles != null ? String.join(", ", motsCles) : "N/A");
    }

    @Override
    public void afficherResume() {
        System.out.println(
                "Résumé du magazine \"" + titre + "\": " + (resume != null ? resume : "Aucun résumé disponible."));
    }

    @Override
    public String toString() {
        return super.toString() + " - Magazine n°" + numero;
    }

}
