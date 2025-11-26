package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.enums.TypeDocument;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class These extends Document {
    private String auteurPrincipal;
    private String directeurRecherche;
    private String universite;
    private String discipline;
    private LocalDate dateSoutenance;
    private String typeAcces;

    public These(int idDocument, String titre, List<String> auteurs, int anneePublication,
            String editeur, String resume, Categorie categorie, List<String> motsCles,
            String langue, String auteurPrincipal, String directeurRecherche,
            String universite, String discipline, LocalDate dateSoutenance, String typeAcces) {
        super(idDocument, titre, auteurs, anneePublication, editeur, resume, categorie, motsCles, langue,
                TypeDocument.THESE);
        this.auteurPrincipal = auteurPrincipal;
        this.directeurRecherche = directeurRecherche;
        this.universite = universite;
        this.discipline = discipline;
        this.dateSoutenance = dateSoutenance;
        this.typeAcces = typeAcces;
        this.typeDocument = TypeDocument.THESE;
    }

    // Getters et setters
    public String getAuteurPrincipal() {
        return auteurPrincipal;
    }

    public void setAuteurPrincipal(String auteurPrincipal) {
        this.auteurPrincipal = auteurPrincipal;
    }

    public String getDirecteurRecherche() {
        return directeurRecherche;
    }

    public void setDirecteurRecherche(String directeurRecherche) {
        this.directeurRecherche = directeurRecherche;
    }

    public String getUniversite() {
        return universite;
    }

    public void setUniversite(String universite) {
        this.universite = universite;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public LocalDate getDateSoutenance() {
        return dateSoutenance;
    }

    public void setDateSoutenance(LocalDate dateSoutenance) {
        this.dateSoutenance = dateSoutenance;
    }

    public String getTypeAcces() {
        return typeAcces;
    }

    public void setTypeAcces(String typeAcces) {
        this.typeAcces = typeAcces;
    }

    // Méthodes abstraites
    @Override
    public String getDescriptionComplete() {
        return String.format(
                "These[id=%d, titre='%s', auteur='%s', directeur='%s', universite='%s', discipline='%s', date='%s', acces='%s']",
                idDocument, titre, auteurPrincipal, directeurRecherche, universite, discipline, dateSoutenance,
                typeAcces);
    }

    @Override
    public void afficherResume() {
        System.out.println("Résumé de la thèse : " + super.resume);
    }

    // Dans com.sgeb.sgbd.model.These

    @Override
    public String toString() {

        // 1. Gère l'affichage de la date
        String dateStr = (dateSoutenance != null) ? dateSoutenance.toString() : "Date inconnue";

        // 2. Formatage complet des informations de la Thèse
        return String.format(
                "Thèse [ID: %d] : %s\n" +
                        "  Auteur: %s | Directeur: %s\n" +
                        "  Université: %s (%s)\n" +
                        "  Soutenue le: %s | Accès: %s\n" +
                        "  Discipline: %s",
                idDocument,
                titre,
                (auteurPrincipal != null && !auteurPrincipal.isEmpty() ? auteurPrincipal : "N/A"),
                (directeurRecherche != null && !directeurRecherche.isEmpty() ? directeurRecherche : "N/A"),
                (universite != null && !universite.isEmpty() ? universite : "N/A"),
                anneePublication,
                dateStr,
                (typeAcces != null && !typeAcces.isEmpty() ? typeAcces : "N/A"),
                (discipline != null && !discipline.isEmpty() ? discipline : "N/A"));
    }

    // NOUVEAU SETTER SURCHARGE pour accepter un String depuis le TextField
    public void setDateSoutenance(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            this.dateSoutenance = null;
            return;
        }
        try {
            // Utilise le parser par défaut : YYYY-MM-DD)
            this.dateSoutenance = LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            System.err.println("Avertissement: Format de date invalide pour la soutenance: " + dateStr);

        }
    }
}
