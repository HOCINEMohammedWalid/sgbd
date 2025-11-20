package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.enums.TypeDocument;
import com.sgeb.sgbd.model.enums.Categorie;
import java.util.List;
import java.util.Objects;

public abstract class Document implements Comparable<Document> {
    protected int idDocument;
    protected String titre;
    protected List<String> auteurs;
    protected int anneePublication;
    protected String editeur;
    protected String resume;
    protected Categorie categorie;
    protected List<String> motsCles;
    protected String langue;
    protected TypeDocument typeDocument;

    protected Document(int idDocument, String titre) {
        this.idDocument = idDocument;
        this.titre = titre;
    }

    protected Document(int idDocument,
            String titre,
            List<String> auteurs,
            int anneePublication,
            String editeur,
            String resume,
            Categorie categorie,
            List<String> motsCles,
            String langue,
            TypeDocument typeDocument) {
        this.idDocument = idDocument;
        this.titre = titre;
        this.auteurs = auteurs;
        this.anneePublication = anneePublication;
        this.editeur = editeur;
        this.resume = resume;
        this.categorie = categorie;
        this.motsCles = motsCles;
        this.langue = langue;
        this.typeDocument = typeDocument;
    }

    // Getters
    public int getIdDocument() {
        return idDocument;
    }

    public String getTitre() {
        return titre;
    }

    public List<String> getAuteurs() {
        return auteurs;
    }

    public int getAnneePublication() {
        return anneePublication;
    }

    public String getEditeur() {
        return editeur;
    }

    public String getResume() {
        return resume;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public List<String> getMotsCles() {
        return motsCles;
    }

    public String getLangue() {
        return langue;
    }

    public TypeDocument getTypeDocument() {
        return typeDocument;
    }

    // Setters
    public void setIdDocument(int idDocument) {
        this.idDocument = idDocument;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setAuteurs(List<String> auteurs) {
        this.auteurs = auteurs;
    }

    public void setAnneePublication(int anneePublication) {
        this.anneePublication = anneePublication;
    }

    public void setEditeur(String editeur) {
        this.editeur = editeur;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public void setMotsCles(List<String> motsCles) {
        this.motsCles = motsCles;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public void setTypeDocument(TypeDocument typeDocument) {
        this.typeDocument = typeDocument;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s)", idDocument, titre, typeDocument);
    }

    // Redéfinition de equals : deux documents sont égaux si leur id est le même
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Document document = (Document) obj;
        return idDocument == document.idDocument;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDocument);
    }

    // compareTo : tri par titre (ordre alphabétique)
    @Override
    public int compareTo(Document other) {
        return this.titre.compareToIgnoreCase(other.titre);
    }

    public void addAuteur(String auteur) {
        if (!auteurs.contains(auteur)) {
            auteurs.add(auteur);
        }
    }

    // Retirer un auteur
    public void removeAuteur(String auteur) {
        auteurs.remove(auteur);
    }

    // Ajouter un mot-clé
    public void addMotCle(String motCle) {
        if (!motsCles.contains(motCle)) {
            motsCles.add(motCle);
        }
    }

    // Retirer un mot-clé
    public void removeMotCle(String motCle) {
        motsCles.remove(motCle);
    }

    // Vérifie si un auteur est présent
    public boolean containsAuteur(String auteur) {
        return auteurs.contains(auteur);
    }

    // Vérifie si un mot-clé est présent
    public boolean containsMotCle(String motCle) {
        return motsCles.contains(motCle);
    }

    // Méthodes abstraites utiles
    public abstract String getDescriptionComplete(); // Description détaillée selon type

    public abstract void afficherResume();
}
