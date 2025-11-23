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

    private final EmpruntDAO empruntDAO;
    private final DocumentDAO documentDAO;

    public AdherentDAO(EmpruntDAO empruntDAO, DocumentDAO documentDAO) {

        this.empruntDAO = empruntDAO;
        this.documentDAO = documentDAO;
    }

    // --- CREATE ---

    public void save(Adherent a) throws SQLException {
        String sql = "INSERT INTO adherent(id, nom, prenom, email, adresse, telephone, date_inscription, statut) "
                + "VALUES (NULL, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                Statement st = conn.createStatement()) {
            conn.setAutoCommit(false);

            ps.setString(1, a.getNom());
            ps.setString(2, a.getPrenom());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getAdresse());
            ps.setString(5, a.getTelephone());
            ps.setString(6, a.getDateInscription().toString());
            ps.setString(7, a.getStatut().name());

            ps.executeUpdate();

            // récupérer l'id généré par SQLite
            try (ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    a.setIdAdherent(rs.getInt(1));
                }
            }
            conn.commit();
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
                    empruntDAO.findByAdherent(id, documentDAO, this));

            return Optional.of(a);
        }
    }

    public Optional<Adherent> findByIdSansP_H(int id) throws SQLException {

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

                    null);

            return Optional.of(a);
        }
    }

    // --- READ ALL ---
    public List<Adherent> findAll() throws SQLException {
        List<Adherent> list = new ArrayList<>();

        String sql = "SELECT * FROM adherent";

        try (Connection conn = Database.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                int id = rs.getInt("id");

                Adherent a = new Adherent(
                        id,
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        LocalDate.parse(rs.getString("date_inscription")),
                        StatutAdherent.valueOf(rs.getString("statut")),

                        empruntDAO.findByAdherent(id, documentDAO, this));

                list.add(a);
            }
        }

        return list;
    }

    // --- UPDATE ---
    public void update(Adherent a) throws SQLException {

        String sql = "UPDATE adherent SET nom=?, prenom=?, email=?, adresse=?, telephone=?, "
                + "date_inscription=?, statut=? WHERE id=?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            ps.setString(1, a.getNom());
            ps.setString(2, a.getPrenom());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getAdresse());
            ps.setString(5, a.getTelephone());
            ps.setString(6, a.getDateInscription().toString());
            ps.setString(7, a.getStatut().name());
            ps.setInt(8, a.getIdAdherent());

            ps.executeUpdate();
            conn.commit();
        }
    }

    // --- DELETE ---
    public void delete(int id) throws SQLException {

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM adherent WHERE id = ?")) {

            conn.setAutoCommit(false);

            ps.setInt(1, id);
            ps.executeUpdate();

            conn.commit();
        }
    }

    public List<Adherent> search(Adherent filtre) throws SQLException {
        List<Adherent> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT id FROM adherent WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (filtre.getNom() != null && !filtre.getNom().isEmpty()) {
            sql.append(" AND nom LIKE ?");
            params.add("%" + filtre.getNom() + "%");
        }

        if (filtre.getPrenom() != null && !filtre.getPrenom().isEmpty()) {
            sql.append(" AND prenom LIKE ?");
            params.add("%" + filtre.getPrenom() + "%");
        }

        if (filtre.getEmail() != null && !filtre.getEmail().isEmpty()) {
            sql.append(" AND email LIKE ?");
            params.add("%" + filtre.getEmail() + "%");
        }

        if (filtre.getAdresse() != null && !filtre.getAdresse().isEmpty()) {
            sql.append(" AND adresse LIKE ?");
            params.add("%" + filtre.getAdresse() + "%");
        }

        if (filtre.getTelephone() != null && !filtre.getTelephone().isEmpty()) {
            sql.append(" AND telephone LIKE ?");
            params.add("%" + filtre.getTelephone() + "%");
        }

        if (filtre.getDateInscription() != null) {
            sql.append(" AND date_inscription = ?");
            params.add(filtre.getDateInscription().toString());
        }

        if (filtre.getStatut() != null) {
            sql.append(" AND statut = ?");
            params.add(filtre.getStatut().name());
        }

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                findById(rs.getInt("id")).ifPresent(list::add);
            }
        }

        return list;
    }

}
