package com.sgeb.sgbd.model;

import com.sgeb.sgbd.dao.AdherentDAO;
import com.sgeb.sgbd.dao.DocumentDAO;
import com.sgeb.sgbd.dao.EmpruntDAO;
import com.sgeb.sgbd.dao.PenaliteDAO;
import com.sgeb.sgbd.model.exception.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AdherentManager {

    private final AdherentDAO dao;
    private final PenaliteDAO penaliteDAO;
    private final EmpruntDAO empruntDAO;
    private final DocumentDAO documentDAO;
    private final double penaliteParJour = 0.50; // montant par jour de retard

    public AdherentManager(PenaliteDAO penaliteDAO, EmpruntDAO empruntDAO, DocumentDAO documentDAO) {
        this.dao = new AdherentDAO(penaliteDAO, empruntDAO, documentDAO);
        this.documentDAO = documentDAO;
        this.penaliteDAO = penaliteDAO;
        this.empruntDAO = empruntDAO;
    }

    // -----------------------------
    // Ajouter un adhérent
    // -----------------------------
    public void ajouterAdherent(Adherent a) throws AdherentExistantException, SQLException {
        if (a == null)
            throw new IllegalArgumentException("Adhérent invalide.");

        Optional<Adherent> existant = dao.findById(a.getIdAdherent());
        if (existant.isPresent())
            throw new AdherentExistantException(a.getIdAdherent());

        dao.save(a);
    }

    // -----------------------------
    // Rechercher
    // -----------------------------
    public Adherent rechercherParId(int id) throws AdherentInexistantException, SQLException {
        return dao.findById(id)
                .orElseThrow(() -> new AdherentInexistantException(id));
    }

    public List<Adherent> rechercherParNom(String nom) throws SQLException {
        if (nom == null || nom.isEmpty())
            return Collections.emptyList();

        return dao.findAll().stream()
                .filter(a -> (a.getNom() + " " + a.getPrenom()).toLowerCase().contains(nom.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Adherent> rechercherMultiCritere(Predicate<Adherent> critere) throws SQLException {
        return dao.findAll().stream()
                .filter(critere)
                .collect(Collectors.toList());
    }

    // -----------------------------
    // Modifier
    // -----------------------------
    public void modifierAdherent(int id, Consumer<Adherent> modification)
            throws AdherentInexistantException, ModificationAdherentException, SQLException {

        Adherent a = dao.findById(id).orElseThrow(() -> new AdherentInexistantException(id));
        if (modification == null)
            throw new ModificationAdherentException("Modification invalide.");

        modification.accept(a);
        dao.update(a); // mise à jour dans la base
    }

    // -----------------------------
    // Historique
    // -----------------------------
    public void afficherHistorique(int id) throws AdherentInexistantException, SQLException {
        Adherent a = dao.findById(id).orElseThrow(() -> new AdherentInexistantException(id));
        a.afficherHistorique();
    }

    // -----------------------------
    // Statut et pénalités
    // -----------------------------
    public boolean peutEmprunter(int id) throws AdherentInexistantException, SQLException {
        Adherent a = dao.findById(id).orElseThrow(() -> new AdherentInexistantException(id));
        return a.peutEmprunter();
    }

    public boolean aPenalites(int id) throws AdherentInexistantException, SQLException {
        Adherent a = dao.findById(id).orElseThrow(() -> new AdherentInexistantException(id));
        return a.aPenalites();
    }

    // -----------------------------
    // Appliquer automatiquement les pénalités sur les retards
    // -----------------------------
    public void appliquerPenalitesRetard() throws SQLException, AdherentInexistantException {
        List<Emprunt> tousEmprunts = empruntDAO.findAll(documentDAO, dao); // tous les emprunts en cours
        LocalDate today = LocalDate.now();

        for (Emprunt e : tousEmprunts) {
            if (e.getDateRetourReelle() == null && e.getDateRetourPrevue().isBefore(today)) {
                long joursRetard = ChronoUnit.DAYS.between(e.getDateRetourPrevue(), today);
                double montant = joursRetard * penaliteParJour;

                Penalite p = new Penalite(montant, "Retard sur '" + e.getDocument().getTitre() + "'", today);

                Adherent a = rechercherParId(e.getAdherent().getIdAdherent());
                a.ajouterPenalite(p);
                penaliteDAO.save(a.getIdAdherent(), p); // enregistrer en base
            }
        }
    }

    // -----------------------------
    // Lister tous
    // -----------------------------
    public List<Adherent> listerTous() throws SQLException {
        return dao.findAll();
    }

    public int taille() throws SQLException {
        return dao.findAll().size();
    }
}
