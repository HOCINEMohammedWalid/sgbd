package com.sgeb.sgbd.dao;

import com.sgeb.sgbd.model.Emprunt;
import com.sgeb.sgbd.model.enums.StatutAdherent;
import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmpruntDAO {

    public void insert(Emprunt e) throws SQLException {
        String sql = "INSERT INTO emprunt(document_id, adherent_id, date_emprunt, date_retour_prevue, date_retour_reelle, penalite) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            ps.setInt(1, e.getDocument().getIdDocument());
            ps.setInt(2, e.getAdherent().getIdAdherent());
            ps.setString(3, e.getDateEmprunt().toString());
            ps.setString(4, e.getDateRetourPrevue().toString());
            ps.setString(5, e.getDateRetourReelle() != null ? e.getDateRetourReelle().toString() : null);
            ps.setDouble(6, e.getPenalite());

            ps.executeUpdate();

            // Récupérer l'ID généré via last_insert_rowid()
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT last_insert_rowid() AS id")) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    e.setIdEmprunt(id); // Assure-toi que Emprunt a un setter pour l'id
                }
            }
            conn.commit();
        }
    }

    // --- READ ---
    public Optional<Emprunt> findById(int id, DocumentDAO documentDAO, AdherentDAO adherentDAO) throws SQLException {
        String sql = "SELECT * FROM emprunt WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();

                Document doc = documentDAO.findById(rs.getInt("document_id")).orElse(null);
                Adherent adh = adherentDAO.findByIdSansP_H(rs.getInt("adherent_id")).orElse(null);
                LocalDate dateEmprunt = LocalDate.parse(rs.getString("date_emprunt"));
                LocalDate dateRetourPrevue = LocalDate.parse(rs.getString("date_retour_prevue"));
                LocalDate dateRetourReelle = rs.getString("date_retour_reelle") != null
                        ? LocalDate.parse(rs.getString("date_retour_reelle"))
                        : null;
                double penalite = rs.getDouble("penalite");
                boolean payee = rs.getInt("penalite_payee") == 1;
                conn.commit();
                return Optional
                        .of(new Emprunt(id, doc, adh, dateEmprunt, dateRetourPrevue, dateRetourReelle, penalite,
                                payee));
            }
        }
    }

    public List<Emprunt> findAll(DocumentDAO documentDAO, AdherentDAO adherentDAO) throws SQLException {
        List<Emprunt> list = new ArrayList<>();

        String sql = "SELECT id FROM emprunt";

        try (Connection conn = Database.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            conn.setAutoCommit(false);
            System.out.println("rrrrrrrrrrrrrr");
            while (rs.next()) {
                findById(rs.getInt("id"), documentDAO, adherentDAO).ifPresent(list::add);
            }
            System.out.println("rrrrrrrrrrrrrr");
            conn.commit();
        }
        System.out.println("rrrrrrrrrrrrrr");
        System.out.println(list.get(0));
        return list;
    }

    // --- UPDATE ---
    public void update(Emprunt e) throws SQLException {
        String sql = "UPDATE emprunt SET date_retour_reelle=?, penalite=?, penalite_payee=? WHERE id=?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            ps.setString(1, e.getDateRetourReelle() != null ? e.getDateRetourReelle().toString() : null);
            ps.setDouble(2, e.getPenalite());
            ps.setInt(3, e.isPayee() ? 1 : 0);
            ps.setInt(4, e.getIdEmprunt());

            ps.executeUpdate();
            conn.commit();
        }
    }

    // --- DELETE ---
    public void delete(Emprunt e) throws SQLException {
        String sql = "DELETE FROM emprunt WHERE id=?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            ps.setInt(1, e.getIdEmprunt());
            ps.executeUpdate();
            conn.commit();
        }
    }

    // --- Emprunts par adhérent ---
    public List<Emprunt> findByAdherent(int adherentId, DocumentDAO documentDAO, AdherentDAO adherentDAO)
            throws SQLException {
        List<Emprunt> list = new ArrayList<>();
        String sql = "SELECT id FROM emprunt WHERE adherent_id=?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            ps.setInt(1, adherentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    findById(rs.getInt("id"), documentDAO, adherentDAO).ifPresent(list::add);
                }
            }
            conn.commit();
        }
        return list;
    }

}
