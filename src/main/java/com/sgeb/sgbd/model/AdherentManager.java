package com.sgeb.sgbd.model;

import com.sgeb.sgbd.dao.AdherentDAO;
import com.sgeb.sgbd.dao.DocumentDAO;
import com.sgeb.sgbd.dao.EmpruntDAO;

import com.sgeb.sgbd.model.enums.StatutAdherent;
import com.sgeb.sgbd.model.exception.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AdherentManager {

    private final AdherentDAO dao;

    private final EmpruntDAO empruntDAO;
    private final DocumentDAO documentDAO;
    private static final double PENALITE_PAR_JOUR = 0.5;

    public AdherentManager(EmpruntDAO empruntDAO, DocumentDAO documentDAO) {
        this.dao = new AdherentDAO(empruntDAO, documentDAO);
        this.documentDAO = documentDAO;

        this.empruntDAO = empruntDAO;
    }

    // -----------------------------
    // Ajouter un adhérent
    // -----------------------------
    public void ajouterAdherent(Adherent a) throws SQLException {
        if (a == null)
            throw new IllegalArgumentException("Adhérent invalide.");

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
                double montant = joursRetard * PENALITE_PAR_JOUR;

                e.setPenalite(montant);
                empruntDAO.update(e);

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

    public List<Adherent> rechercherAvance(Adherent filtre) throws SQLException {
        if (filtre == null)
            return Collections.emptyList();

        return dao.search(filtre);
    }

    public static void main(String[] args) {
        try {

            EmpruntDAO empruntDAO = new EmpruntDAO();
            DocumentDAO documentDAO = new DocumentDAO();
            AdherentManager manager = new AdherentManager(empruntDAO, documentDAO);

            // -----------------------------
            // Créer des adhérents fixes
            // -----------------------------
            Adherent a1 = new Adherent(1, "Dupont", "Jean", "jean.dupont@example.com",
                    "12 rue de Paris", "0123456789", LocalDate.of(2023, 1, 15),
                    StatutAdherent.ACTIF, new ArrayList<>());

            Adherent a2 = new Adherent(2, "Martin", "Claire", "claire.martin@example.com",
                    "34 avenue de Lyon", "0987654321", LocalDate.of(2022, 9, 5),
                    StatutAdherent.ACTIF, new ArrayList<>());

            manager.ajouterAdherent(a1);
            manager.ajouterAdherent(a2);

            System.out.println("Catalogue adhérents initial : " + manager.taille());

            // -----------------------------
            // Rechercher par nom
            // -----------------------------
            List<Adherent> resultNom = manager.rechercherParNom("Dupont");
            System.out.println("Recherche par nom 'Dupont' : " + resultNom.size() + " résultat(s)");

            // -----------------------------
            // Modifier un adhérent
            // -----------------------------
            manager.modifierAdherent(a1.getIdAdherent(), a -> {
                a.setEmail("jean.dupont@newmail.com");
                a.setAdresse("15 rue de Marseille");
            });

            Adherent modifie = manager.rechercherParId(a1.getIdAdherent());
            System.out.println("Email après modification : " + modifie.getEmail());
            System.out.println("Adresse après modification : " + modifie.getAdresse());

            // -----------------------------
            // Recherche avancée
            // -----------------------------
            Adherent filtre = new Adherent();
            filtre.setNom("Martin");
            List<Adherent> resultAvance = manager.rechercherAvance(filtre);
            System.out.println("Recherche avancée : " + resultAvance.size() + " résultat(s)");

            // -----------------------------
            // Appliquer pénalités
            // -----------------------------
            manager.appliquerPenalitesRetard();
            System.out.println("Pénalités appliquées aux adhérents.");

            // -----------------------------
            // Lister tous
            // -----------------------------
            List<Adherent> tous = manager.listerTous();
            System.out.println("Tous les adhérents :");
            for (Adherent a : tous) {
                System.out.println(a.getIdAdherent() + " - " + a.getNom() + " " + a.getPrenom());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
