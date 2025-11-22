package com.sgeb.sgbd.dao;

import com.sgeb.sgbd.util.*;
import com.sgeb.sgbd.model.*;
import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.enums.TypeDocument;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DocumentDAO {

    // -------------------- CREATE --------------------
    public void insert(Document doc) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO document (id, titre, type, auteurs, annee_publication, editeur, resume, categorie, mots_cles, langue) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, doc.getIdDocument());
                ps.setString(2, doc.getTitre());
                ps.setString(3, doc.getTypeDocument().name());
                ps.setString(4, doc.getAuteurs() != null ? String.join(",", doc.getAuteurs()) : null);
                ps.setObject(5, doc.getAnneePublication() > 0 ? doc.getAnneePublication() : null);
                ps.setString(6, doc.getEditeur());
                ps.setString(7, doc.getResume());
                ps.setString(8, doc.getCategorie() != null ? doc.getCategorie().name() : null);
                ps.setString(9, doc.getMotsCles() != null ? String.join(",", doc.getMotsCles()) : null);
                ps.setString(10, doc.getLangue());
                ps.executeUpdate();
            }
        }

        insertSpecific(doc);
    }

    private void insertSpecific(Document doc) throws SQLException {
        try (Connection conn = Database.getConnection()) {
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
                    ps.setString(4, m.getDatePublication() != null ? m.getDatePublication().toString() : null);
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
                    ps.setString(6, t.getDateSoutenance() != null ? t.getDateSoutenance().toString() : null);
                    ps.setString(7, t.getTypeAcces());
                    ps.executeUpdate();
                }
            }
        }
    }

    // -------------------- READ --------------------
    public Optional<Document> findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM document WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                TypeDocument type = TypeDocument.valueOf(rs.getString("type"));
                switch (type) {
                    case LIVRE:
                        return Optional.of(readLivre(id));
                    case MAGAZINE:
                        return Optional.of(readMagazine(id));
                    case DVD:
                        return Optional.of(readDVD(id));
                    case EBOOK:
                        return Optional.of(readEBook(id));
                    case ARTICLE:
                        return Optional.of(readArticle(id));
                    case THESE:
                        return Optional.of(readThese(id));
                    default:
                        return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    // Implementations readXxx (readLivre, readMagazine...) sont similaires à
    // readLivre de mon exemple précédent.
    // Elles récupèrent les champs de la table `document` et de la table
    // spécialisée.
    private DocumentData readDocumentBase(int id) throws SQLException {

        String sql = "SELECT * FROM document WHERE id=?";
        Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
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
        d.categorie = rs.getString("categorie") != null
                ? Categorie.valueOf(rs.getString("categorie"))
                : null;
        d.motsCles = splitList(rs.getString("mots_cles"));
        d.langue = rs.getString("langue");

        return d;
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
        return (csv == null || csv.isEmpty())
                ? new ArrayList<>()
                : Arrays.asList(csv.split(","));
    }

    private Livre readLivre(int id) throws SQLException {

        // Lecture des attributs généraux dans la table "document"
        DocumentData base = readDocumentBase(id);
        if (base == null)
            return null;

        String sql = "SELECT * FROM livre WHERE id=?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return null;

            String isbn = rs.getString("isbn");
            int nbPages = rs.getInt("nb_pages");
            String collection = rs.getString("collection");

            // Construction du Livre selon TON constructeur
            return new Livre(
                    id,
                    base.titre,
                    base.auteurs,
                    base.annee,
                    base.editeur,
                    base.resume,
                    base.categorie,
                    base.motsCles,
                    base.langue,
                    isbn,
                    nbPages,
                    collection);
        }
    }

    private Magazine readMagazine(int id) throws SQLException {

        // Lecture de la partie commune (table document)
        DocumentData base = readDocumentBase(id);
        if (base == null)
            return null;

        String sql = "SELECT numero, periodicite, date_publication FROM magazine WHERE id=?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return null;

            int numero = rs.getInt("numero");
            String periodicite = rs.getString("periodicite");

            LocalDate datePublication = null;
            String dp = rs.getString("date_publication");
            if (dp != null)
                datePublication = LocalDate.parse(dp);

            return new Magazine(
                    id,
                    base.titre,
                    base.auteurs,
                    base.annee,
                    base.editeur,
                    base.resume,
                    base.categorie,
                    base.motsCles,
                    base.langue,
                    numero,
                    periodicite,
                    datePublication);
        }
    }

    private DVD readDVD(int id) throws SQLException {

        // Lecture de la partie commune (table document)
        DocumentData base = readDocumentBase(id);
        if (base == null)
            return null;

        String sql = "SELECT * FROM dvd WHERE id=?";

        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return null;

            String realisateur = rs.getString("realisateur");
            int duree = rs.getInt("duree");
            String classification = rs.getString("classification");

            return new DVD(
                    id,
                    base.titre,
                    realisateur,
                    duree,
                    classification);
        }
    }

    private EBook readEBook(int id) throws SQLException {
        DocumentData base = readDocumentBase(id); // récupère les champs communs à Document
        if (base == null)
            return null;

        String sql = "SELECT * FROM ebook WHERE id=?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;

            String urlAcces = rs.getString("url_acces");
            String format = rs.getString("format");
            boolean drm = rs.getBoolean("drm");

            // Crée l'objet avec le constructeur existant
            EBook e = new EBook(id, base.titre, urlAcces, format, drm);

            // Complète les champs hérités de Document
            e.setAuteurs(base.auteurs);
            e.setResume(base.resume);
            e.setAnneePublication(base.annee); // base.annee correspond à anneePublication
            e.setEditeur(base.editeur);
            e.setLangue(base.langue);
            e.setCategorie(base.categorie);
            e.setMotsCles(base.motsCles);

            return e;
        }
    }

    private ArticleUniversitaire readArticle(int id) throws SQLException {

        DocumentData base = readDocumentBase(id);
        if (base == null)
            return null;

        String sql = "SELECT * FROM article WHERE id=?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;

            String journal = rs.getString("journal");
            int volume = rs.getInt("volume");
            int numero = rs.getInt("numero");
            String pages = rs.getString("pages");
            String doi = rs.getString("doi"); // <-- récupérer DOI

            ArticleUniversitaire a = new ArticleUniversitaire(
                    id, base.titre, journal, volume, numero, pages, doi); // <-- passer DOI
            a.setResume(base.resume);
            a.setAnneePublication(base.annee);
            a.setEditeur(base.editeur);
            a.setLangue(base.langue);
            a.setCategorie(base.categorie);
            a.setMotsCles(base.motsCles);

            return a;
        }
    }

    private These readThese(int id) throws SQLException {
        DocumentData base = readDocumentBase(id);
        if (base == null)
            return null;

        String sql = "SELECT * FROM these WHERE id=?";
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;

            String auteur = rs.getString("auteur_principal");
            String directeur = rs.getString("directeur_recherche");
            String universite = rs.getString("universite");
            String discipline = rs.getString("discipline");
            LocalDate dateSoutenance = rs.getDate("date_soutenance").toLocalDate();
            String typeAcces = rs.getString("type_acces");

            These t = new These(id, base.titre, auteur, directeur, universite, discipline, dateSoutenance, typeAcces);
            t.setResume(base.resume);
            t.setAnneePublication(base.annee);
            t.setEditeur(base.editeur);
            t.setLangue(base.langue);
            t.setCategorie(base.categorie);
            t.setMotsCles(base.motsCles);

            return t;
        }
    }

    // -------------------- DELETE --------------------
    public void delete(Document doc) throws SQLException {
        try (Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM document WHERE id=?")) {
            ps.setInt(1, doc.getIdDocument());
            ps.executeUpdate();
        }
    }

    // -------------------- LIST ALL --------------------
    public List<Document> findAll() throws SQLException {
        List<Document> docs = new ArrayList<>();
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id FROM document")) {
            while (rs.next()) {
                findById(rs.getInt("id")).ifPresent(docs::add);
            }
        }
        return docs;
    }
}
