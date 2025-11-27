package com.sgeb.sgbd.dao;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.enums.StatutAdherent;
import com.sgeb.sgbd.util.Database;
import com.sgeb.sgbd.model.Emprunt;

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

    // --- CREATE (1: Save standard) ---
    /**
     * Enregistre un nouvel adhérent (utilisé pour les insertions simples ou
     * internes).
     * Le mot de passe haché est mis à NULL.
     */
    public void save(Adherent a) throws SQLException {
        // La colonne mot_de_passe_hache est remplie avec NULL.
        String sql = "INSERT INTO adherent(id, nom, prenom, email, adresse, telephone, date_inscription, statut, mot_de_passe_hache) "
                + "VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, NULL)";

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

            // Récupération de l'ID généré (méthode SQLite)
            try (ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    a.setIdAdherent(rs.getInt(1)); // Assurez-vous d'utiliser la bonne méthode (setId ou setIdAdherent)
                }
            }
            conn.commit();
        }
    }

    // --- CREATE (2: Save avec Hash - Pour l'inscription) ---
    /**
     * Enregistre un nouvel Adhérent et son mot de passe haché. Compatible SQLite.
     *
     * @param a               L'objet Adherent à enregistrer.
     * @param motDePasseHache Le mot de passe haché.
     */
    public void saveWithPasswordHash(Adherent a, String motDePasseHache) throws SQLException {
        String sql = "INSERT INTO adherent(id, nom, prenom, email, adresse, telephone, date_inscription, statut, mot_de_passe_hache) "
                + "VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            ps.setString(8, motDePasseHache);

            ps.executeUpdate();

            // Récupération de l'ID généré (Spécifique à SQLite)
            try (ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    a.setIdAdherent(rs.getInt(1)); // Assurez-vous d'utiliser la bonne méthode
                }
            }
            conn.commit();
        }
    }

    // --- READ ---

    // Lit l'adhérent avec tous ses emprunts (chargement complet)
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

    /**
     * Lit l'adhérent complet (pour la session) en utilisant son email.
     */
    public Optional<Adherent> findByEmail(String email) throws SQLException {
        // 1. Trouver l'ID de l'adhérent en utilisant l'email
        String sql = "SELECT id FROM adherent WHERE email = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return Optional.empty();

            // 2. Utiliser l'ID trouvé pour charger l'objet Adherent complet
            // (En réutilisant findById, on s'assure que les emprunts sont chargés)
            int id = rs.getInt("id");
            return findById(id);
        }
    }

    // Utilisé pour lire l'adhérent sans charger ses emprunts
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
                    new ArrayList<Emprunt>()); // Liste d'emprunts vide

            return Optional.of(a);
        }
    }

    // Méthode pour l'authentification
    public Optional<String[]> findByEmailWithHash(String email) throws SQLException {
        String sql = "SELECT email, mot_de_passe_hache FROM adherent WHERE email = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return Optional.empty();

            String retrievedEmail = rs.getString("email");
            String motDePasseHache = rs.getString("mot_de_passe_hache");

            return Optional.of(new String[] { retrievedEmail, motDePasseHache });
        }
    }

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

        // Note: Le mot_de_passe_hache n'est pas mis à jour ici.
        String sql = "UPDATE adherent SET nom=?, prenom=?, email=?, adresse=?, telephone=?, statut=? WHERE id=?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            ps.setString(1, a.getNom());
            ps.setString(2, a.getPrenom());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getAdresse());
            ps.setString(5, a.getTelephone());
            ps.setString(6, a.getStatut().name());
            ps.setInt(7, a.getIdAdherent()); // Utilisez getIdAdherent() ou a.getId()

            ps.executeUpdate();
            conn.commit();
        }
    }
    // Dans AdherentDAO.java, après la méthode update(Adherent a)

    // -----------------------------
    // Mise à jour du mot de passe
    // -----------------------------
    /**
     * Met à jour uniquement la colonne mot_de_passe_hache pour un adhérent
     * spécifique.
     * 
     * @param idAdherent        L'identifiant de l'adhérent.
     * @param newHashedPassword Le nouveau mot de passe haché.
     * @throws SQLException Si une erreur de base de données survient.
     */
    public void updatePassword(int idAdherent, String newHashedPassword) throws SQLException {

        // Requête ciblée uniquement sur la colonne de mot de passe
        final String SQL = "UPDATE adherent SET mot_de_passe_hache = ? WHERE id = ?";

        // Le code utilise Database.getConnection() comme dans le reste de votre classe
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {

            conn.setAutoCommit(false); // Utiliser les transactions comme dans vos autres méthodes

            ps.setString(1, newHashedPassword); // Le nouveau hash
            ps.setInt(2, idAdherent); // L'ID de l'adhérent

            int rowsAffected = ps.executeUpdate();

            conn.commit();

            if (rowsAffected == 0) {
                throw new SQLException(
                        "Échec de la mise à jour du mot de passe: Adhérent ID " + idAdherent + " non trouvé.");
            }

        }
    }

    // --- SEARCH (MÉTHODE MANQUANTE CORRIGÉE) ---
    /**
     * Recherche des adhérents correspondant aux critères spécifiés dans l'objet
     * filtre.
     * * @param filtre L'objet Adherent contenant les critères de recherche.
     * 
     * @return Une liste d'Adherents correspondant aux critères.
     */
    public List<Adherent> search(Adherent filtre) throws SQLException {
        List<Adherent> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT id FROM adherent WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // -- Logique de construction de la requête --
        if (filtre.getNom() != null && !filtre.getNom().isEmpty()) {
            sql.append(" AND nom LIKE ?");
            params.add("%" + filtre.getNom() + "%");
        }

        if (filtre.getPrenom() != null && !filtre.getPrenom().isEmpty()) {
            sql.append(" AND prenom LIKE ?");
            params.add("%" + filtre.getPrenom() + "%");
        }

        // Vous pouvez ajouter d'autres filtres ici (email, statut, etc.)

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Utilise findById() qui charge l'objet complet avec ses emprunts
                findById(rs.getInt("id")).ifPresent(list::add);
            }
        }
        return list;
    }

    public void delete(int id) throws SQLException {

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM adherent WHERE id = ?")) {

            conn.setAutoCommit(false);

            ps.setInt(1, id);
            ps.executeUpdate();

            conn.commit();
        }
    }
}