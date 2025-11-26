package com.sgeb.sgbd.model;

import java.util.List;

import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.enums.TypeDocument;

public class EBook extends Document {
    private String urlAcces;
    private String format;
    private boolean drm;

    public EBook(int idDocument, String titre, List<String> auteurs,
            int anneePublication,
            String editeur,
            String resume,
            Categorie categorie,
            List<String> motsCles,
            String langue, String urlAcces, String format, boolean drm) {
        super(idDocument, titre, auteurs,
                anneePublication,
                editeur,
                resume,
                categorie,
                motsCles,
                langue,
                TypeDocument.EBOOK);
        this.urlAcces = urlAcces;
        this.format = format;
        this.drm = drm;

    }

    // Getters et setters
    public String getUrlAcces() {
        return urlAcces;
    }

    public void setUrlAcces(String urlAcces) {
        this.urlAcces = urlAcces;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean hasDrm() {
        return drm;
    }

    public void setDrm(boolean drm) {
        this.drm = drm;
    }

    // Méthodes abstraites de Document
    @Override
    public String getDescriptionComplete() {
        return String.format("EBook[id=%d, titre='%s', url='%s', format='%s', DRM=%b]",
                idDocument, titre, urlAcces, format, drm);
    }

    @Override
    public void afficherResume() {
        System.out.println("Résumé de l'EBook : " + super.resume);
    }

    // Dans com.sgeb.sgbd.model.EBook

    @Override
    public String toString() {

        // 1. Gère l'affichage du statut DRM
        String drmStatus = drm ? "OUI (Protection)" : "NON";

        // 2. Simplifie l'URL pour la lisibilité (affiche juste le début ou un
        // indicateur)
        String urlSummary;
        if (urlAcces != null && urlAcces.length() > 30) {
            urlSummary = urlAcces.substring(0, 30) + "...";
        } else if (urlAcces != null && !urlAcces.isEmpty()) {
            urlSummary = urlAcces;
        } else {
            urlSummary = "Lien non spécifié";
        }

        // 3. Formatage complet des informations de l'EBook
        return String.format(
                "EBook [ID: %d] : %s\n" +
                        "  Format: %s | DRM: %s\n" +
                        "  Accès: %s\n" +
                        "  Éditeur: %s | Année: %d",
                idDocument,
                titre,
                (format != null && !format.isEmpty() ? format : "Inconnu"),
                drmStatus,
                urlSummary,
                (editeur != null && !editeur.isEmpty() ? editeur : "N/A"),
                anneePublication);
    }
}
