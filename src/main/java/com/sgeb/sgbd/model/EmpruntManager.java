package com.sgeb.sgbd.model;

import com.sgeb.sgbd.dao.EmpruntDAO;

import com.sgeb.sgbd.dao.DocumentDAO;
import com.sgeb.sgbd.dao.AdherentDAO;
import com.sgeb.sgbd.model.enums.StatutAdherent;
import com.sgeb.sgbd.model.exception.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class EmpruntManager {

    private final EmpruntDAO empruntDAO;
    private final DocumentDAO documentDAO;
    private final AdherentDAO adherentDAO;
    private final int limiteEmprunts = 5;
    private final int dureeEmpruntJours = 21;
    private final double penaliteParJour = 0.50;

    public EmpruntManager(EmpruntDAO empruntDAO, DocumentDAO documentDAO, AdherentDAO adherentDAO) {
        this.empruntDAO = empruntDAO;
        this.documentDAO = documentDAO;
        this.adherentDAO = adherentDAO;
    }

    // -----------------------------
    // Emprunter un document
    // -----------------------------
    public void emprunter(Document doc, Adherent adherent) throws EmpruntException, SQLException {
        if (doc == null || adherent == null)
            throw new IllegalArgumentException("Document ou adhérent invalide.");

        if (!adherent.peutEmprunter())
            throw new StatutAdherentException();

        long nbEmprunts = empruntDAO.findByAdherent(adherent.getIdAdherent(), documentDAO, adherentDAO).size();
        if (nbEmprunts >= limiteEmprunts)
            throw new LimiteEmpruntsAtteinteException();

        // Vérifier si le document est disponible
        boolean dejaEmprunte = empruntDAO.findAll(documentDAO, adherentDAO).stream()
                .anyMatch(e -> e.getDocument().equals(doc));
        if (dejaEmprunte)
            throw new DocumentIndisponibleException();

        LocalDate dateEmprunt = LocalDate.now();
        LocalDate dateRetourPrevue = dateEmprunt.plusDays(dureeEmpruntJours);

        Emprunt e = new Emprunt(0, doc, adherent, dateEmprunt, dateRetourPrevue);
        doc.setDispo(false);
        documentDAO.update(doc);
        empruntDAO.insert(e);

        adherent.ajouterEmprunt(e);

    }

    // -----------------------------
    // Retour d’un emprunt
    // -----------------------------
    public void retour(Emprunt e) throws EmpruntException, SQLException {
        if (e == null)
            throw new EmpruntInexistantException();

        LocalDate dateRetour = LocalDate.now();
        e.setDateRetourReelle(dateRetour);

        long retard = ChronoUnit.DAYS.between(e.getDateRetourPrevue(), dateRetour);
        e.setPenalite(retard > 0 ? retard * penaliteParJour : 0);
        e.getDocument().setDispo(true);
        empruntDAO.update(e);
    }

    // -----------------------------
    // Emprunts en cours
    // -----------------------------
    public List<Emprunt> listerEmpruntsEnCours() throws SQLException {
        return empruntDAO.findAll(documentDAO, adherentDAO).stream()
                .filter(e -> e.getDateRetourReelle() == null)
                .sorted(Comparator.comparing(Emprunt::getDateEmprunt))
                .collect(Collectors.toList());
    }

    public List<Emprunt> listerEmprunts() throws SQLException {

        return empruntDAO.findAll(documentDAO, adherentDAO).stream()
                .sorted(Comparator.comparing(Emprunt::getDateEmprunt))
                .collect(Collectors.toList());
    }

    // -----------------------------
    // Emprunts en retard
    // -----------------------------
    public List<Emprunt> empruntsEnRetard() throws SQLException {
        LocalDate today = LocalDate.now();
        return empruntDAO.findAll(documentDAO, adherentDAO).stream()
                .filter(e -> e.getDateRetourReelle() == null)
                .filter(e -> e.getDateRetourPrevue().isBefore(today))
                .collect(Collectors.toList());
    }

    // -----------------------------
    // Compter les emprunts d’un adhérent
    // -----------------------------
    public long nombreEmpruntsEnCours(Adherent adherent) throws SQLException {
        return empruntDAO.findByAdherent(adherent.getIdAdherent(), documentDAO, adherentDAO).stream()
                .filter(e -> e.getDateRetourReelle() == null)
                .count();
    }

    public List<Emprunt> listerEmpruntsParAdherent(int idAdherent) throws SQLException {
        return empruntDAO.findByAdherent(idAdherent, documentDAO, adherentDAO).stream()
                .sorted(Comparator.comparing(Emprunt::getDateEmprunt).reversed()) // Tri par date décroissante
                .collect(Collectors.toList());
    }

    public void payerPenalite(Emprunt e) throws SQLException, EmpruntException {
        if (e == null)
            throw new EmpruntInexistantException();

        if (e.getPenalite() <= 0)
            throw new EmpruntException("Aucune pénalité à payer.");

        if (e.isPayee())
            throw new EmpruntException("La pénalité est déjà payée.");

        e.marquerPenalitePayee();
        empruntDAO.update(e);

    }

    public static void main(String[] args) {
        try {
            // DAO

            DocumentDAO documentDAO = new DocumentDAO();
            EmpruntDAO empruntDAO = new EmpruntDAO();

            AdherentDAO adherentDAO = new AdherentDAO(empruntDAO, documentDAO);
            // Managers
            EmpruntManager empruntManager = new EmpruntManager(empruntDAO, documentDAO, adherentDAO);

            // Créer un adhérent pour test
            Adherent adh = new Adherent(1, "Dupont", "Jean", "jean.dupont@example.com",
                    "12 rue de Paris", "0123456789", LocalDate.of(2023, 1, 15),
                    StatutAdherent.ACTIF, new ArrayList<>());

            adherentDAO.save(adh);
            System.out.println(adh.getIdAdherent());

            // Récupérer un document existant (id 1 par exemple)
            Document doc = documentDAO.findById(8).orElseThrow(() -> new RuntimeException("Document introuvable"));

            // Emprunter le document
            System.out.println(doc);
            System.out.println(adh);
            empruntManager.emprunter(doc, adh);
            System.out.println("Document emprunté avec succès !");

            // Lister les emprunts en cours
            List<Emprunt> enCours = empruntManager.listerEmpruntsEnCours();
            System.out.println(enCours);
            System.out.println("Emprunts en cours : " + enCours.size());

            // Retourner le document
            Emprunt e = enCours.get(0);
            empruntManager.retour(e);
            System.out.println("Document retourné, pénalité : " + e.getPenalite());

            // Vérifier les emprunts en retard
            List<Emprunt> enRetard = empruntManager.empruntsEnRetard();
            System.out.println("Emprunts en retard : " + enRetard.size());

        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Erreur : " + ex.getMessage());
        }
    }
}
