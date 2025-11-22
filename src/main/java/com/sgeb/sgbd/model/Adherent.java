package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.enums.StatutAdherent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Adherent {
    private int idAdherent;
    private String nom;
    private String prenom;
    private String email;
    private String adresse;
    private String telephone;
    private LocalDate dateInscription;
    private StatutAdherent statut;
    private List<Penalite> penalites;
    private List<Emprunt> historiqueEmprunts;

    // Constructeur complet
    public Adherent(int idAdherent, String nom, String prenom, String email, String adresse, String telephone,
            LocalDate dateInscription, StatutAdherent statut,
            List<Penalite> penalites, List<Emprunt> historiqueEmprunts) {
        this.idAdherent = idAdherent;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.adresse = adresse;
        this.telephone = telephone;
        this.dateInscription = dateInscription != null ? dateInscription : LocalDate.now();
        this.statut = statut != null ? statut : StatutAdherent.ACTIF;
        this.penalites = penalites != null ? penalites : new ArrayList<>();
        this.historiqueEmprunts = historiqueEmprunts != null ? historiqueEmprunts : new ArrayList<>();
    }

    // Vérifie si l'adhérent peut emprunter
    public boolean peutEmprunter() {
        if (statut != StatutAdherent.ACTIF)
            return false;
        for (Penalite p : penalites) {
            if (!p.isPayee())
                return false;
        }
        return true;
    }

    // Ajouter une pénalité
    public void ajouterPenalite(Penalite p) {
        penalites.add(p);
    }

    // Ajouter un emprunt à l'historique
    public void ajouterEmprunt(Emprunt e) {
        historiqueEmprunts.add(e);
    }

    // Afficher l'historique des emprunts
    public void afficherHistorique() {
        if (historiqueEmprunts.isEmpty()) {
            System.out.println(getNomComplet() + " n'a aucun emprunt enregistré.");
        } else {
            System.out.println("Historique des emprunts de " + getNomComplet() + " :");
            for (Emprunt e : historiqueEmprunts) {
                System.out.println(e);
            }
        }
    }

    // Vérifie si l'adhérent a des pénalités non payées
    public boolean aPenalites() {
        for (Penalite p : penalites) {
            if (!p.isPayee())
                return true;
        }
        return false;
    }

    // Getters et setters
    public int getIdAdherent() {
        return idAdherent;
    }

    public void setIdAdherent(int idAdherent) {
        this.idAdherent = idAdherent;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNomComplet() {
        return nom + " " + prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public StatutAdherent getStatut() {
        return statut;
    }

    public void setStatut(StatutAdherent statut) {
        this.statut = statut;
    }

    public List<Penalite> getPenalites() {
        return penalites;
    }

    public void setPenalites(List<Penalite> penalites) {
        this.penalites = penalites;
    }

    public List<Emprunt> getHistoriqueEmprunts() {
        return historiqueEmprunts;
    }

    public void setHistoriqueEmprunts(List<Emprunt> historiqueEmprunts) {
        this.historiqueEmprunts = historiqueEmprunts;
    }

    @Override
    public String toString() {
        return String.format("Adh[%d] %s %s - Statut: %s", idAdherent, nom, prenom, statut);
    }
}
