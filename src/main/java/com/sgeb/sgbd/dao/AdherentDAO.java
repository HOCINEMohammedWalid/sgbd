package com.sgeb.sgbd.dao;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.enums.StatutAdherent;
import com.sgeb.sgbd.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdherentDAO {
    private final PenaliteDAO penaliteDAO;
    private final EmpruntDAO empruntDAO;

    public AdherentDAO(PenaliteDAO penaliteDAO, EmpruntDAO empruntDAO) {
        this.penaliteDAO = penaliteDAO;
        this.empruntDAO = empruntDAO;
    }

    // --- CREATE ---
    public void save(Adherent a) throws SQLException {
        String sql = "INSERT INTO adherent(id, nom, prenom, email, adresse, telephone, date_inscription, statut) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getIdAdherent());
            ps.setString(2, a.getNom());
            ps.setString(3, a.getPrenom());
            ps.setString(4, a.getEmail());
            ps.setString(5, a.getAdresse());
            ps.setString(6, a.getTelephone());
            ps.setString(7, a.getDateInscription().toString());
            ps.setString(8, a.getStatut().name());
            ps.executeUpdate();
        }
    }

    // --- READ ---
    public Optional<Adherent> findById(int id) throws SQLException {
        String sql = "SELECT * FROM adherent WHERE id = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return Optional.empty();

            Adherent a = new Adherent(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("adresse"),
                    rs.getString("telephone"),
                    LocalDate.parse(rs.getString("date_inscription")),
                    StatutAdherent.valueOf(rs.getString("statut")),
                    penaliteDAO.findByAdherentId(rs.getInt("id")), // charge les pénalités
                    empruntDAO.findByAdherentId(rs.getInt("id")) // charge les emprunts
            );

            return Optional.of(a);
        }
    }

    public List<Adherent> findAll() throws SQLException {
        List<Adherent> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT id FROM adherent")) {

            while (rs.next()) {
                findById(rs.getInt("id")).ifPresent(list::add);
            }
        }
        return list;
    }

    // --- UPDATE ---
    public void update(Adherent a) throws SQLException {
        String sql = "UPDATE adherent SET nom=?, prenom=?, email=?, adresse=?, telephone=?, "
                + "date_inscription=?, statut=? WHERE id=?";

        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getNom());
            ps.setString(2, a.getPrenom());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getAdresse());
            ps.setString(5, a.getTelephone());
            ps.setString(6, a.getDateInscription().toString());
            ps.setString(7, a.getStatut().name());
            ps.setInt(8, a.getIdAdherent());

            ps.executeUpdate();
        }
    }

    // --- DELETE ---
    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM adherent WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
