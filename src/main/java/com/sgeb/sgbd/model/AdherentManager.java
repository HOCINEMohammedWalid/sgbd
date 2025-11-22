package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.exception.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AdherentManager {

    private final Map<Integer, Adherent> adherents = new HashMap<>();

    // -----------------------------
    // Ajouter un adhérent
    // -----------------------------
    public void ajouterAdherent(Adherent a) throws AdherentExistantException {
        if (a == null)
            throw new IllegalArgumentException("Adhérent invalide.");
        if (adherents.containsKey(a.getIdAdherent()))
            throw new AdherentExistantException(a.getIdAdherent());
        adherents.put(a.getIdAdherent(), a);
    }

    // -----------------------------
    // Rechercher
    // -----------------------------
    public Adherent rechercherParId(int id) throws AdherentInexistantException {
        Adherent a = adherents.get(id);
        if (a == null)
            throw new AdherentInexistantException(id);
        return a;
    }

    public List<Adherent> rechercherParNom(String nom) {
        if (nom == null || nom.isEmpty())
            return Collections.emptyList();
        return adherents.values().stream()
                .filter(a -> (a.getNom() + " " + a.getPrenom()).toLowerCase().contains(nom.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Adherent> rechercherMultiCritere(Predicate<Adherent> critere) {
        return adherents.values().stream()
                .filter(critere)
                .collect(Collectors.toList());
    }

    // -----------------------------
    // Modifier
    // -----------------------------
    public void modifierAdherent(int id, Consumer<Adherent> modification)
            throws AdherentInexistantException, ModificationAdherentException {
        Adherent a = adherents.get(id);
        if (a == null)
            throw new AdherentInexistantException(id);
        if (modification == null)
            throw new ModificationAdherentException("Modification invalide.");
        modification.accept(a);
    }

    // -----------------------------
    // Historique
    // -----------------------------
    public void afficherHistorique(int id) throws AdherentInexistantException {
        Adherent a = adherents.get(id);
        if (a == null)
            throw new AdherentInexistantException(id);
        a.afficherHistorique();
    }

    // -----------------------------
    // Statut et pénalités
    // -----------------------------
    public boolean peutEmprunter(int id) throws AdherentInexistantException {
        Adherent a = adherents.get(id);
        if (a == null)
            throw new AdherentInexistantException(id);
        return a.peutEmprunter();
    }

    public boolean aPenalites(int id) throws AdherentInexistantException {
        Adherent a = adherents.get(id);
        if (a == null)
            throw new AdherentInexistantException(id);
        return a.aPenalites();
    }

    // -----------------------------
    // Lister tous
    // -----------------------------
    public List<Adherent> listerTous() {
        return new ArrayList<>(adherents.values());
    }

    public int taille() {
        return adherents.size();
    }
}
