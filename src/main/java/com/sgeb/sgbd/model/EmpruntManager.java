package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.Emprunt;
import com.sgeb.sgbd.model.exception.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class EmpruntManager {

    private final Map<Integer, Emprunt> empruntsEnCours = new HashMap<>();
    private final int limiteEmprunts = 5;
    private final int dureeEmpruntJours = 21; // 3 semaines
    private final double penaliteParJour = 0.50;

    // -----------------------------
    // Enregistrer un emprunt
    // -----------------------------
    public void emprunter(Document doc, Adherent adherent) throws EmpruntException {
        if (doc == null || adherent == null)
            throw new IllegalArgumentException("Document ou adhérent invalide.");

        if (!adherent.peutEmprunter())
            throw new StatutAdherentException();

        if (nombreEmpruntsEnCours(adherent) >= limiteEmprunts)
            throw new LimiteEmpruntsAtteinteException();

        // Vérifier si le document est disponible
        boolean dejaEmprunte = empruntsEnCours.values().stream()
                .anyMatch(e -> e.getDocument().equals(doc));
        if (dejaEmprunte)
            throw new DocumentIndisponibleException();

        LocalDate dateEmprunt = LocalDate.now();
        LocalDate dateRetourPrevue = dateEmprunt.plusDays(dureeEmpruntJours);

        Emprunt e = new Emprunt(doc, adherent, dateEmprunt, dateRetourPrevue);
        empruntsEnCours.put(e.getIdEmprunt(), e);
        adherent.ajouterEmprunt(e);
    }

    // -----------------------------
    // Enregistrer un retour
    // -----------------------------
    public void retour(Emprunt e) throws EmpruntException {
        if (e == null || !empruntsEnCours.containsKey(e.getIdEmprunt()))
            throw new EmpruntInexistantException();

        LocalDate dateRetour = LocalDate.now();
        e.setDateRetourReelle(dateRetour);

        // Vérifier retard
        long retard = ChronoUnit.DAYS.between(e.getDateRetourPrevue(), dateRetour);
        e.setPenalite(retard > 0 ? retard * penaliteParJour : 0);

        empruntsEnCours.remove(e.getIdEmprunt());
    }

    // -----------------------------
    // Emprunts en cours
    // -----------------------------
    public List<Emprunt> listerEmpruntsEnCours() {
        return empruntsEnCours.values().stream()
                .sorted(Comparator.comparing(Emprunt::getDateEmprunt))
                .collect(Collectors.toList());
    }

    // -----------------------------
    // Alertes sur les retards
    // -----------------------------
    public List<Emprunt> empruntsEnRetard() {
        LocalDate today = LocalDate.now();
        return empruntsEnCours.values().stream()
                .filter(e -> e.getDateRetourPrevue().isBefore(today))
                .collect(Collectors.toList());
    }

    // -----------------------------
    // Compter les emprunts en cours d’un adhérent
    // -----------------------------
    public long nombreEmpruntsEnCours(Adherent adherent) {
        return empruntsEnCours.values().stream()
                .filter(e -> e.getAdherent().equals(adherent))
                .count();
    }
}
