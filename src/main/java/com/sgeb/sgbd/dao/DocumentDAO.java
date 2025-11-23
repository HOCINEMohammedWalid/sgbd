package com.sgeb.sgbd.dao;

import com.sgeb.sgbd.model.*;
import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.enums.TypeDocument;
import com.sgeb.sgbd.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DocumentDAO {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    // -------------------- CREATE --------------------
    public void insert(Document doc) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // transaction

            String sql = "INSERT INTO document (titre, type, auteurs, annee_publication, editeur, resume, categorie, mots_cles, langue) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, doc.getTitre());
                ps.setString(2, doc.getTypeDocument().name());
                ps.setString(3, doc.getAuteurs() != null ? String.join(",", doc.getAuteurs()) : null);
                ps.setObject(4, doc.getAnneePublication() > 0 ? doc.getAnneePublication() : null);
                ps.setString(5, doc.getEditeur());
                ps.setString(6, doc.getResume());
                ps.setString(7, doc.getCategorie() != null ? doc.getCategorie().name() : null);
                ps.setString(8, doc.getMotsCles() != null ? String.join(",", doc.getMotsCles()) : null);
                ps.setString(9, doc.getLangue());

                ps.executeUpdate();

                // récupération de l'ID inséré
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        doc.setIdDocument(rs.getInt(1));
                    }
                }
            }

            insertSpecific(doc, conn);
            conn.commit();
        }
    }

    private void insertSpecific(Document doc, Connection conn) throws SQLException {
        if (doc instanceof Livre) {
            Livre l = (Livre) doc;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO livre (id, isbn, nb_pages, collection) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, l.getIdDocument());
                ps.setString(2, l.getISBN());
                ps.setInt(3, l.getNbPages());
                ps.setString(4, l.getCollection());
                ps.executeUpdate();
            }
        } else if (doc instanceof Magazine) {
            Magazine m = (Magazine) doc;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO magazine (id, numero, periodicite, date_publication) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, m.getIdDocument());
                ps.setInt(2, m.getNumero());
                ps.setString(3, m.getPeriodicite());
                ps.setString(4, m.getDatePublication() != null ? m.getDatePublication().format(DATE_FORMAT) : null);
                ps.executeUpdate();
            }
        } else if (doc instanceof DVD) {
            DVD d = (DVD) doc;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO dvd (id, realisateur, duree, classification) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, d.getIdDocument());
                ps.setString(2, d.getRealisateur());
                ps.setInt(3, d.getDuree());
                ps.setString(4, d.getClassification());
                ps.executeUpdate();
            }
        } else if (doc instanceof EBook) {
            EBook e = (EBook) doc;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO ebook (id, url_acces, format, drm) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, e.getIdDocument());
                ps.setString(2, e.getUrlAcces());
                ps.setString(3, e.getFormat());
                ps.setInt(4, e.hasDrm() ? 1 : 0);
                ps.executeUpdate();
            }
        } else if (doc instanceof ArticleUniversitaire) {
            ArticleUniversitaire a = (ArticleUniversitaire) doc;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO article_universitaire (id, titre_revue, volume, numero, pages, doi) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, a.getIdDocument());
                ps.setString(2, a.getTitreRevue());
                ps.setInt(3, a.getVolume());
                ps.setInt(4, a.getNumero());
                ps.setString(5, a.getPages());
                ps.setString(6, a.getDOI());
                ps.executeUpdate();
            }
        } else if (doc instanceof These) {
            These t = (These) doc;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO these (id, auteur_principal, directeur_recherche, universite, discipline, date_soutenance, type_acces) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, t.getIdDocument());
                ps.setString(2, t.getAuteurPrincipal());
                ps.setString(3, t.getDirecteurRecherche());
                ps.setString(4, t.getUniversite());
                ps.setString(5, t.getDiscipline());
                ps.setString(6, t.getDateSoutenance() != null ? t.getDateSoutenance().format(DATE_FORMAT) : null);
                ps.setString(7, t.getTypeAcces());
                ps.executeUpdate();
            }
        }
    }

    // -------------------- READ --------------------
    public Optional<Document> findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            String sql = "SELECT * FROM document WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    TypeDocument type = TypeDocument.valueOf(rs.getString("type"));
                    switch (type) {
                        case LIVRE:
                            return Optional.of(readLivre(id, conn));
                        case MAGAZINE:
                            return Optional.of(readMagazine(id, conn));
                        case DVD:
                            return Optional.of(readDVD(id, conn));
                        case EBOOK:
                            return Optional.of(readEBook(id, conn));
                        case ARTICLE:
                            return Optional.of(readArticle(id, conn));
                        case THESE:
                            return Optional.of(readThese(id, conn));
                        default:
                            return Optional.empty();
                    }
                }
            }
            conn.commit();
        }
        return Optional.empty();
    }

    // -------------------- READ Helpers --------------------
    private DocumentData readDocumentBase(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM document WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;

            DocumentData d = new DocumentData();
            d.id = id;
            d.titre = rs.getString("titre");
            d.auteurs = splitList(rs.getString("auteurs"));
            d.annee = rs.getInt("annee_publication");
            d.editeur = rs.getString("editeur");
            d.resume = rs.getString("resume");
            d.categorie = rs.getString("categorie") != null ? Categorie.valueOf(rs.getString("categorie")) : null;
            d.motsCles = splitList(rs.getString("mots_cles"));
            d.langue = rs.getString("langue");
            return d;
        }
    }

    private static class DocumentData {
        int id;
        String titre;
        List<String> auteurs;
        int annee;
        String editeur;
        String resume;
        Categorie categorie;
        List<String> motsCles;
        String langue;
    }

    private List<String> splitList(String csv) {
        return (csv == null || csv.isEmpty()) ? new ArrayList<>() : Arrays.asList(csv.split(","));
    }

    private Livre readLivre(int id, Connection conn) throws SQLException {
        DocumentData base = readDocumentBase(id, conn);
        if (base == null)
            return null;

        String sql = "SELECT * FROM livre WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;
            return new Livre(id, base.titre, base.auteurs, base.annee, base.editeur,
                    base.resume, base.categorie, base.motsCles, base.langue,
                    rs.getString("isbn"), rs.getInt("nb_pages"), rs.getString("collection"));
        }
    }

    private Magazine readMagazine(int id, Connection conn) throws SQLException {
        DocumentData base = readDocumentBase(id, conn);
        if (base == null)
            return null;

        String sql = "SELECT numero, periodicite, date_publication FROM magazine WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;
            LocalDate datePublication = rs.getString("date_publication") != null
                    ? LocalDate.parse(rs.getString("date_publication"), DATE_FORMAT)
                    : null;
            return new Magazine(id, base.titre, base.auteurs, base.annee, base.editeur,
                    base.resume, base.categorie, base.motsCles, base.langue,
                    rs.getInt("numero"), rs.getString("periodicite"), datePublication);
        }
    }

    private DVD readDVD(int id, Connection conn) throws SQLException {
        DocumentData base = readDocumentBase(id, conn);
        if (base == null)
            return null;

        String sql = "SELECT * FROM dvd WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;
            return new DVD(id, base.titre, base.auteurs, base.annee, base.editeur,
                    base.resume, base.categorie, base.motsCles, base.langue,
                    rs.getString("realisateur"), rs.getInt("duree"), rs.getString("classification"));
        }
    }

    private EBook readEBook(int id, Connection conn) throws SQLException {
        DocumentData base = readDocumentBase(id, conn);
        if (base == null)
            return null;

        String sql = "SELECT * FROM ebook WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;
            return new EBook(id, base.titre, base.auteurs, base.annee, base.editeur, base.resume,
                    base.categorie, base.motsCles, base.langue,
                    rs.getString("url_acces"), rs.getString("format"), rs.getBoolean("drm"));
        }
    }

    private ArticleUniversitaire readArticle(int id, Connection conn) throws SQLException {
        DocumentData base = readDocumentBase(id, conn);
        if (base == null)
            return null;

        String sql = "SELECT * FROM article_universitaire WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;
            return new ArticleUniversitaire(id, base.titre, base.auteurs, base.annee, base.editeur,
                    base.resume, base.categorie, base.motsCles, base.langue,
                    rs.getString("titre_revue"), rs.getInt("volume"), rs.getInt("numero"),
                    rs.getString("pages"), rs.getString("doi"));
        }
    }

    private These readThese(int id, Connection conn) throws SQLException {
        DocumentData base = readDocumentBase(id, conn);
        if (base == null)
            return null;

        String sql = "SELECT * FROM these WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;
            LocalDate dateSoutenance = rs.getString("date_soutenance") != null
                    ? LocalDate.parse(rs.getString("date_soutenance"), DATE_FORMAT)
                    : null;
            return new These(id, base.titre, base.auteurs, base.annee, base.editeur,
                    base.resume, base.categorie, base.motsCles, base.langue,
                    rs.getString("auteur_principal"), rs.getString("directeur_recherche"),
                    rs.getString("universite"), rs.getString("discipline"),
                    dateSoutenance, rs.getString("type_acces"));
        }
    }

    // -------------------- DELETE --------------------
    public void delete(Document doc) throws SQLException {
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM document WHERE id=?")) {
            conn.setAutoCommit(false);
            ps.setInt(1, doc.getIdDocument());
            ps.executeUpdate();
            conn.commit();
        }
    }

    // -------------------- UPDATE --------------------
    public void update(Document doc) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            String sql = "UPDATE document SET titre=?, type=?, auteurs=?, annee_publication=?, " +
                    "editeur=?, resume=?, categorie=?, mots_cles=?, langue=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, doc.getTitre());
                ps.setString(2, doc.getTypeDocument().name());
                ps.setString(3, doc.getAuteurs() != null ? String.join(",", doc.getAuteurs()) : null);
                ps.setObject(4, doc.getAnneePublication() > 0 ? doc.getAnneePublication() : null);
                ps.setString(5, doc.getEditeur());
                ps.setString(6, doc.getResume());
                ps.setString(7, doc.getCategorie() != null ? doc.getCategorie().name() : null);
                ps.setString(8, doc.getMotsCles() != null ? String.join(",", doc.getMotsCles()) : null);
                ps.setString(9, doc.getLangue());
                ps.setInt(10, doc.getIdDocument());
                ps.executeUpdate();
            }

            updateSpecific(doc, conn);
            conn.commit();
        }
    }

    private void updateSpecific(Document doc, Connection conn) throws SQLException {
        // similaire à insertSpecific mais avec UPDATE
        if (doc instanceof Livre) {
            Livre l = (Livre) doc;
            try (PreparedStatement ps = conn
                    .prepareStatement("UPDATE livre SET isbn=?, nb_pages=?, collection=? WHERE id=?")) {
                ps.setString(1, l.getISBN());
                ps.setInt(2, l.getNbPages());
                ps.setString(3, l.getCollection());
                ps.setInt(4, l.getIdDocument());
                ps.executeUpdate();
            }
        }
        // faire pareil pour Magazine, DVD, EBook, ArticleUniversitaire, These
        // utilise la même logique que insertSpecific
    }

    // -------------------- LIST ALL --------------------
    public List<Document> findAll() throws SQLException {
        List<Document> docs = new ArrayList<>();
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id FROM document")) {
            conn.setAutoCommit(false);
            while (rs.next()) {
                int id = rs.getInt("id");
                findById(id).ifPresent(docs::add);
            }
            conn.commit();
        }
        return docs;
    }
}
