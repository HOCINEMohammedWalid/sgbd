package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.enums.EtatExemplaire;
import com.sgeb.sgbd.model.enums.SupportType;

public class Exemplaire {
    private int idExemplaire;
    private Document document;
    private String codeBarre;
    private EtatExemplaire etat;
    private SupportType support;

    public Exemplaire(int idExemplaire, Document document, String codeBarre, SupportType support) {
        this.idExemplaire = idExemplaire;
        this.document = document;
        this.codeBarre = codeBarre;
        this.support = support;
        this.etat = EtatExemplaire.DISPONIBLE;
    }

    // Getters et setters
    public int getIdExemplaire() {
        return idExemplaire;
    }

    public void setIdExemplaire(int idExemplaire) {
        this.idExemplaire = idExemplaire;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getCodeBarre() {
        return codeBarre;
    }

    public void setCodeBarre(String codeBarre) {
        this.codeBarre = codeBarre;
    }

    public EtatExemplaire getEtat() {
        return etat;
    }

    public void setEtat(EtatExemplaire etat) {
        this.etat = etat;
    }

    public SupportType getSupport() {
        return support;
    }

    public void setSupport(SupportType support) {
        this.support = support;
    }

    // Méthodes utilitaires
    public boolean estDisponible() {
        return etat == EtatExemplaire.DISPONIBLE;
    }

    public void marquerEmprunte() {
        this.etat = EtatExemplaire.EMPRUNTE;
    }

    public void marquerDisponible() {
        this.etat = EtatExemplaire.DISPONIBLE;
    }

    // equals et hashCode basés sur l'ID et le code-barres
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Exemplaire))
            return false;
        Exemplaire other = (Exemplaire) obj;
        return idExemplaire == other.idExemplaire &&
                (codeBarre != null ? codeBarre.equals(other.codeBarre) : other.codeBarre == null);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(idExemplaire);
        result = 31 * result + (codeBarre != null ? codeBarre.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Exemplaire[%d] %s - %s (%s)", idExemplaire, document.getTitre(), etat, support);
    }
}
