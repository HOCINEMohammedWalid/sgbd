package com.sgeb.sgbd.util;

import com.sgeb.sgbd.dao.*;
import com.sgeb.sgbd.model.*;
import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.enums.StatutAdherent;

import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;

public class Database {

        private static final String URL = "jdbc:sqlite:sgbd.db";

        public static Connection getConnection() throws SQLException {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:sgbd.db");
                conn.setAutoCommit(false);
                try (Statement st = conn.createStatement()) {
                        st.execute("PRAGMA foreign_keys = ON");
                }
                return conn;
        }

        // ====================================================================
        // CRÉATION DES TABLES
        // ====================================================================
        public static void initialize() {
                try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

                        // ------------------ Table Adherent ------------------
                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS adherent (" +
                                                        "id INTEGER PRIMARY KEY," +
                                                        "nom TEXT NOT NULL," +
                                                        "prenom TEXT NOT NULL," +
                                                        "email TEXT," +
                                                        "adresse TEXT," +
                                                        "telephone TEXT," +
                                                        "date_inscription TEXT NOT NULL," +
                                                        "statut TEXT NOT NULL ," +
                                                        "mot_de_passe_hache TEXT" +
                                                        ");");

                        // ------------------ Table Document ------------------
                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS document (" +
                                                        "id INTEGER PRIMARY KEY," +
                                                        "titre TEXT NOT NULL," +
                                                        "type TEXT NOT NULL," +
                                                        "auteurs TEXT," +
                                                        "annee_publication INTEGER," +
                                                        "editeur TEXT," +
                                                        "resume TEXT," +
                                                        "categorie TEXT," +
                                                        "mots_cles TEXT," +
                                                        "langue TEXT," +
                                                        "dispo INTEGER" +
                                                        ");");

                        // Sous-tables spécialisées
                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS livre (" +
                                                        "id INTEGER PRIMARY KEY," +
                                                        "isbn TEXT," +
                                                        "nb_pages INTEGER," +
                                                        "collection TEXT," +
                                                        "FOREIGN KEY(id) REFERENCES document(id) ON DELETE CASCADE" +
                                                        ");");

                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS magazine (" +
                                                        "id INTEGER PRIMARY KEY," +
                                                        "numero INTEGER," +
                                                        "periodicite TEXT," +
                                                        "date_publication TEXT," +
                                                        "FOREIGN KEY(id) REFERENCES document(id) ON DELETE CASCADE" +
                                                        ");");

                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS dvd (" +
                                                        "id INTEGER PRIMARY KEY," +
                                                        "realisateur TEXT," +
                                                        "duree INTEGER," +
                                                        "classification TEXT," +
                                                        "FOREIGN KEY(id) REFERENCES document(id) ON DELETE CASCADE" +
                                                        ");");

                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS ebook (" +
                                                        "id INTEGER PRIMARY KEY," +
                                                        "url_acces TEXT," +
                                                        "format TEXT," +
                                                        "drm INTEGER," +
                                                        "FOREIGN KEY(id) REFERENCES document(id) ON DELETE CASCADE" +
                                                        ");");

                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS article_universitaire (" +
                                                        "id INTEGER PRIMARY KEY," +
                                                        "titre_revue TEXT," +
                                                        "volume INTEGER," +
                                                        "numero INTEGER," +
                                                        "pages TEXT," +
                                                        "doi TEXT," +
                                                        "FOREIGN KEY(id) REFERENCES document(id) ON DELETE CASCADE" +
                                                        ");");

                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS these (" +
                                                        "id INTEGER PRIMARY KEY," +
                                                        "auteur_principal TEXT," +
                                                        "directeur_recherche TEXT," +
                                                        "universite TEXT," +
                                                        "discipline TEXT," +
                                                        "date_soutenance TEXT," +
                                                        "type_acces TEXT," +
                                                        "FOREIGN KEY(id) REFERENCES document(id) ON DELETE CASCADE" +
                                                        ");");

                        stmt.executeUpdate(
                                        "CREATE TABLE IF NOT EXISTS emprunt (" +
                                                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                                        "document_id INTEGER NOT NULL," +
                                                        "adherent_id INTEGER NOT NULL," +
                                                        "date_emprunt TEXT NOT NULL," +
                                                        "date_retour_prevue TEXT NOT NULL," +
                                                        "date_retour_reelle TEXT," +

                                                        "penalite REAL," +
                                                        "penalite_payee INTEGER DEFAULT 0," +
                                                        "FOREIGN KEY(document_id) REFERENCES document(id) ON DELETE CASCADE,"
                                                        +
                                                        "FOREIGN KEY(adherent_id) REFERENCES adherent(id) ON DELETE CASCADE"
                                                        +
                                                        ");");

                        System.out.println("Tables initialisées avec succès !");

                        conn.commit();

                } catch (SQLException e) {
                        System.err.println("Erreur lors de l'initialisation des tables : " + e.getMessage());
                }

        }

        // ====================================================================
        // INSERTION AUTOMATIQUE (SI VIDE)
        // ====================================================================
        private static boolean isTableEmpty(String table) throws SQLException {
                try (Connection conn = getConnection();
                                Statement st = conn.createStatement();
                                ResultSet rs = st.executeQuery("SELECT COUNT(*) AS c FROM " + table)) {
                        boolean v = rs.next() && rs.getInt("c") == 0;
                        conn.commit();
                        return v;
                }
        }

        public static void seedDatabase() {
                try (Connection conn = getConnection()) {

                        DocumentDAO documentDAO = new DocumentDAO();

                        Document[] livres = {
                                        new Livre(1, "Le Seigneur des Anneaux", Arrays.asList("J.R.R. Tolkien"), 1954,
                                                        "Allen & Unwin", "Roman de fantasy épique.",
                                                        Categorie.LITTERATURE, Arrays.asList("fantasy", "aventure"),
                                                        "FR", "9780544003415", 1200, "Saga"),
                                        new Livre(2, "1984", Arrays.asList("George Orwell"), 1949, "Secker & Warburg",
                                                        "Dystopie politique.", Categorie.LITTERATURE,
                                                        Arrays.asList("politique", "totalitarisme"), "FR",
                                                        "9780451524935", 328, "Classiques Modernes"),
                                        new Livre(3, "Harry Potter à l'école des sorciers",
                                                        Arrays.asList("J.K. Rowling"), 1997, "Bloomsbury",
                                                        "Premier tome de la saga.", Categorie.LITTERATURE,
                                                        Arrays.asList("magie", "aventure"), "FR", "9780747532699", 223,
                                                        "Saga Harry Potter"),
                                        new Livre(4, "Le Petit Prince", Arrays.asList("Antoine de Saint-Exupéry"), 1943,
                                                        "Reynal & Hitchcock", "Conte poétique.", Categorie.LITTERATURE,
                                                        Arrays.asList("conte", "philosophie"), "FR", "9780156012195",
                                                        96, "Classiques"),
                                        new Livre(5, "Les Misérables", Arrays.asList("Victor Hugo"), 1862, "Pagnerre",
                                                        "Épopée sociale.", Categorie.LITTERATURE,
                                                        Arrays.asList("drame", "société"), "FR", "9782070409185", 1463,
                                                        "Classiques"),
                                        new Livre(6, "Le Comte de Monte-Cristo", Arrays.asList("Alexandre Dumas"), 1844,
                                                        "Michel Lévy Frères", "Roman d'aventure et de vengeance.",
                                                        Categorie.LITTERATURE, Arrays.asList("aventure", "vengeance"),
                                                        "FR", "9782070409321", 1312, "Classiques"),
                                        new Livre(7, "Moby Dick", Arrays.asList("Herman Melville"), 1851,
                                                        "Harper & Brothers", "Roman sur la chasse à la baleine.",
                                                        Categorie.LITTERATURE, Arrays.asList("aventure", "mer"), "FR",
                                                        "9781503280786", 720, "Classiques"),
                                        new Livre(8, "Crime et Châtiment", Arrays.asList("Fiodor Dostoïevski"), 1866,
                                                        "The Russian Messenger",
                                                        "Roman philosophique sur la culpabilité.",
                                                        Categorie.LITTERATURE, Arrays.asList("psychologie", "crime"),
                                                        "FR", "9782070360024", 430, "Classiques"),
                                        new Livre(9, "Les Fleurs du mal", Arrays.asList("Charles Baudelaire"), 1857,
                                                        "Poulet-Malassis", "Recueil de poésie.", Categorie.LITTERATURE,
                                                        Arrays.asList("poésie", "symbolisme"), "FR", "9782070413117",
                                                        256, "Poésie"),
                                        new Livre(10, "Don Quichotte", Arrays.asList("Miguel de Cervantes"), 1605,
                                                        "Francisco de Robles", "Aventures d’un chevalier fou.",
                                                        Categorie.LITTERATURE, Arrays.asList("satire", "aventure"),
                                                        "FR", "9782070360598", 1056, "Classiques")
                        };

                        // ===============================
                        // 10 MAGAZINES
                        // ===============================
                        Document[] magazines = {
                                        new Magazine(11, "Science & Vie", Arrays.asList("Rédaction S&V"), 2024,
                                                        "Reworld Media", "Dossier scientifique général.",
                                                        Categorie.SCIENCE, Arrays.asList("science", "actualité"), "FR",
                                                        1250, "Mensuel", LocalDate.of(2024, 3, 1)),
                                        new Magazine(12, "National Geographic", Arrays.asList("Rédaction NG"), 2024,
                                                        "National Geographic Society",
                                                        "Magazine sur la nature et la science.", Categorie.SCIENCE,
                                                        Arrays.asList("nature", "exploration"), "FR", 202, "Mensuel",
                                                        LocalDate.of(2024, 4, 1)),
                                        new Magazine(13, "Le Monde Diplomatique", Arrays.asList("Rédaction LMD"), 2024,
                                                        "Le Monde", "Analyse géopolitique.", Categorie.SCIENCE,
                                                        Arrays.asList("politique", "géopolitique"), "FR", 121,
                                                        "Mensuel", LocalDate.of(2024, 5, 1)),
                                        new Magazine(14, "Time", Arrays.asList("Rédaction Time"), 2024, "Time USA",
                                                        "Magazine d'actualité mondiale.", Categorie.SCIENCE,
                                                        Arrays.asList("actualité", "international"), "FR", 334,
                                                        "Hebdomadaire", LocalDate.of(2024, 3, 15)),
                                        new Magazine(15, "Forbes", Arrays.asList("Rédaction Forbes"), 2024,
                                                        "Forbes Media", "Magazine économique et business.",
                                                        Categorie.SCIENCE, Arrays.asList("finance", "entreprise"), "FR",
                                                        108, "Mensuel", LocalDate.of(2024, 2, 1)),
                                        new Magazine(16, "Géo", Arrays.asList("Rédaction Géo"), 2024, "GEO SAS",
                                                        "Magazine sur les voyages et cultures.", Categorie.SCIENCE,
                                                        Arrays.asList("voyage", "culture"), "FR", 421, "Mensuel",
                                                        LocalDate.of(2024, 1, 1)),
                                        new Magazine(17, "Vogue", Arrays.asList("Rédaction Vogue"), 2024, "Condé Nast",
                                                        "Mode et tendances.", Categorie.SCIENCE,
                                                        Arrays.asList("mode", "style"), "FR", 987, "Mensuel",
                                                        LocalDate.of(2024, 6, 1)),
                                        new Magazine(18, "Science Magazine", Arrays.asList("Rédaction Science"), 2024,
                                                        "AAAS", "Recherche scientifique avancée.", Categorie.SCIENCE,
                                                        Arrays.asList("recherche", "science"), "FR", 215,
                                                        "Hebdomadaire", LocalDate.of(2024, 5, 15)),
                                        new Magazine(19, "The Economist", Arrays.asList("Rédaction Economist"), 2024,
                                                        "The Economist Group", "Analyse économique.", Categorie.SCIENCE,
                                                        Arrays.asList("économie", "finance"), "FR", 202, "Hebdomadaire",
                                                        LocalDate.of(2024, 3, 1)),
                                        new Magazine(20, "Nature", Arrays.asList("Rédaction Nature"), 2024, "Springer",
                                                        "Journal scientifique.", Categorie.SCIENCE,
                                                        Arrays.asList("science", "recherche"), "FR", 480,
                                                        "Hebdomadaire", LocalDate.of(2024, 4, 1))
                        };

                        // ===============================
                        // 10 DVDs
                        // ===============================
                        Document[] dvds = {
                                        new DVD(21, "Inception", Arrays.asList("Christopher Nolan"), 2010,
                                                        "Warner Bros", "Film de science-fiction.", Categorie.SCIENCE,
                                                        Arrays.asList("sci-fi", "thriller"), "FR", "Christopher Nolan",
                                                        148, "PG-13"),
                                        new DVD(22, "Interstellar", Arrays.asList("Christopher Nolan"), 2014,
                                                        "Paramount", "Exploration spatiale.", Categorie.SCIENCE,
                                                        Arrays.asList("espace", "drame"), "FR", "Christopher Nolan",
                                                        169, "PG-13"),
                                        new DVD(23, "Le Roi Lion", Arrays.asList("Roger Allers & Rob Minkoff"), 1994,
                                                        "Disney", "Classique Disney.", Categorie.LITTERATURE,
                                                        Arrays.asList("animation", "famille"), "FR",
                                                        "Roger Allers & Rob Minkoff", 88, "Tout public"),
                                        new DVD(24, "Matrix", Arrays.asList("Lana & Lilly Wachowski"), 1999,
                                                        "Warner Bros", "Révolution cyberpunk.", Categorie.SCIENCE,
                                                        Arrays.asList("action", "sci-fi"), "FR",
                                                        "Lana & Lilly Wachowski", 136, "R"),
                                        new DVD(25, "Avatar", Arrays.asList("James Cameron"), 2009, "20th Century Fox",
                                                        "Planète Pandora.", Categorie.SCIENCE,
                                                        Arrays.asList("sci-fi", "aventure"), "FR", "James Cameron", 162,
                                                        "PG-13"),
                                        new DVD(26, "Gladiator", Arrays.asList("Ridley Scott"), 2000, "DreamWorks",
                                                        "Épopée romaine.", Categorie.LITTERATURE,
                                                        Arrays.asList("historique", "drame"), "FR", "Ridley Scott", 155,
                                                        "R"),
                                        new DVD(27, "Titanic", Arrays.asList("James Cameron"), 1997, "20th Century Fox",
                                                        "Histoire d'amour tragique.", Categorie.LITTERATURE,
                                                        Arrays.asList("drame", "romance"), "FR", "James Cameron", 195,
                                                        "PG-13"),
                                        new DVD(28, "Jurassic Park", Arrays.asList("Steven Spielberg"), 1993,
                                                        "Universal Pictures", "Dinosaures ressuscités.",
                                                        Categorie.SCIENCE, Arrays.asList("aventure", "action"), "FR",
                                                        "Steven Spielberg", 127, "PG-13"),
                                        new DVD(29, "The Dark Knight", Arrays.asList("Christopher Nolan"), 2008,
                                                        "Warner Bros", "Batman contre le Joker.", Categorie.SCIENCE,
                                                        Arrays.asList("superhero", "action"), "FR", "Christopher Nolan",
                                                        152, "PG-13"),
                                        new DVD(30, "Pulp Fiction", Arrays.asList("Quentin Tarantino"), 1994, "Miramax",
                                                        "Film culte.", Categorie.LITTERATURE,
                                                        Arrays.asList("crime", "drame"), "FR", "Quentin Tarantino", 154,
                                                        "R")
                        };

                        // ===============================
                        // 10 EBOOKS
                        // ===============================
                        Document[] ebooks = {
                                        new EBook(31, "Programmation Java Moderne", Arrays.asList("Jean Dupont"), 2021,
                                                        "Eyrolles", "Guide pratique Java.", Categorie.INFORMATIQUE,
                                                        Arrays.asList("java", "programmation"), "FR",
                                                        "https://ebooks.example.com/java.pdf", "PDF", true),
                                        new EBook(32, "Python pour les Nuls", Arrays.asList("Marie Martin"), 2022,
                                                        "First", "Initiation à Python.", Categorie.INFORMATIQUE,
                                                        Arrays.asList("python", "débutant"), "FR",
                                                        "https://ebooks.example.com/python.epub", "EPUB", false),
                                        new EBook(33, "C++ Avancé", Arrays.asList("Paul Durand"), 2020, "Dunod",
                                                        "Techniques avancées.", Categorie.INFORMATIQUE,
                                                        Arrays.asList("c++", "avancé"), "FR",
                                                        "https://ebooks.example.com/cpp.mobi", "MOBI", true),
                                        new EBook(34, "Développement Web Moderne", Arrays.asList("Sophie Leroy"), 2023,
                                                        "O'Reilly", "Guide HTML/CSS/JS.", Categorie.INFORMATIQUE,
                                                        Arrays.asList("web", "javascript"), "FR",
                                                        "https://ebooks.example.com/web.pdf", "PDF", true),
                                        new EBook(35, "Bases de données SQL", Arrays.asList("Luc Bernard"), 2021,
                                                        "Eyrolles", "SQL pour débutants.", Categorie.INFORMATIQUE,
                                                        Arrays.asList("sql", "bdd"), "FR",
                                                        "https://ebooks.example.com/sql.epub", "EPUB", false),
                                        new EBook(36, "Machine Learning", Arrays.asList("Clara Noël"), 2022, "Springer",
                                                        "Apprentissage automatique.", Categorie.SCIENCE,
                                                        Arrays.asList("ml", "ia"), "FR",
                                                        "https://ebooks.example.com/ml.pdf", "PDF", true),
                                        new EBook(37, "Deep Learning", Arrays.asList("Alexandre Petit"), 2023,
                                                        "Springer", "Réseaux neuronaux.", Categorie.SCIENCE,
                                                        Arrays.asList("dl", "ia"), "FR",
                                                        "https://ebooks.example.com/dl.epub", "EPUB", false),
                                        new EBook(38, "Cybersecurity Essentials", Arrays.asList("Nina Dubois"), 2021,
                                                        "Wiley", "Sécurité informatique.", Categorie.INFORMATIQUE,
                                                        Arrays.asList("cybersecurity", "réseau"), "FR",
                                                        "https://ebooks.example.com/cyber.pdf", "PDF", true),
                                        new EBook(39, "Big Data Analytics", Arrays.asList("Olivier Laurent"), 2022,
                                                        "Packt", "Analyse des données massives.",
                                                        Categorie.INFORMATIQUE, Arrays.asList("bigdata", "data"), "FR",
                                                        "https://ebooks.example.com/bigdata.epub", "EPUB", false),
                                        new EBook(40, "Intelligence Artificielle", Arrays.asList("Emma Leroy"), 2023,
                                                        "Eyrolles", "Introduction à l'IA.", Categorie.SCIENCE,
                                                        Arrays.asList("ia", "apprentissage"), "FR",
                                                        "https://ebooks.example.com/ia.mobi", "MOBI", true)
                        };

                        // ===============================
                        // 10 ARTICLES UNIVERSITAIRES
                        // ===============================
                        Document[] articles = {
                                        new ArticleUniversitaire(41, "Optimisation des Réseaux Neuronaux",
                                                        Arrays.asList("Dr. X"), 2025, "JAI Research", "Étude avancée.",
                                                        Categorie.SCIENCE, Arrays.asList("IA", "réseaux neuronaux"),
                                                        "FR", "Journal of AI Research", 32, 4, "201–225",
                                                        "10.1234/jair.2025.32.4"),
                                        new ArticleUniversitaire(42, "Analyse des Algorithmes Distribués",
                                                        Arrays.asList("Dr. Y"), 2025, "IJ Computing",
                                                        "Algorithmes distribués.", Categorie.SCIENCE,
                                                        Arrays.asList("algorithmique", "réseaux"), "FR",
                                                        "International Journal of Computing", 12, 2, "50–78",
                                                        "10.5678/ijc.2025.12.2"),
                                        new ArticleUniversitaire(43, "Méthodes Modernes de Cryptographie",
                                                        Arrays.asList("Dr. Z"), 2025, "ES Review",
                                                        "Techniques de cryptographie.", Categorie.SCIENCE,
                                                        Arrays.asList("cryptographie", "sécurité"), "FR",
                                                        "European Security Review", 18, 1, "1–30",
                                                        "10.9987/esr.2025.18.1"),
                                        new ArticleUniversitaire(44, "Data Mining Avancé", Arrays.asList("Dr. A"), 2024,
                                                        "Data Science J.", "Techniques de data mining.",
                                                        Categorie.SCIENCE, Arrays.asList("data", "analyse"), "FR",
                                                        "Data Science Journal", 5, 3, "45–60", "10.1111/dsj.2024.5.3"),
                                        new ArticleUniversitaire(45, "Réseaux de Capteurs", Arrays.asList("Dr. B"),
                                                        2023, "Sensors J.", "Capteurs intelligents.", Categorie.SCIENCE,
                                                        Arrays.asList("capteurs", "réseaux"), "FR", "Sensors Journal",
                                                        7, 1, "12–25", "10.2222/sj.2023.7.1"),
                                        new ArticleUniversitaire(46, "Blockchain et Sécurité", Arrays.asList("Dr. C"),
                                                        2024, "Blockchain J.", "Étude sur blockchain.",
                                                        Categorie.SCIENCE, Arrays.asList("blockchain", "sécurité"),
                                                        "FR", "Blockchain Journal", 2, 2, "33–50",
                                                        "10.3333/bj.2024.2.2"),
                                        new ArticleUniversitaire(47, "IoT et Applications", Arrays.asList("Dr. D"),
                                                        2023, "IoT Review", "Internet des objets.", Categorie.SCIENCE,
                                                        Arrays.asList("iot", "réseaux"), "FR", "IoT Review", 10, 5,
                                                        "101–120", "10.4444/iot.2023.10.5"),
                                        new ArticleUniversitaire(48, "Systèmes Distribués", Arrays.asList("Dr. E"),
                                                        2022, "Distributed Computing", "Architecture distribuée.",
                                                        Categorie.SCIENCE, Arrays.asList("systèmes", "réseaux"), "FR",
                                                        "Distributed Computing", 8, 4, "55–80", "10.5555/dc.2022.8.4"),
                                        new ArticleUniversitaire(49, "Cloud Computing", Arrays.asList("Dr. F"), 2023,
                                                        "Cloud J.", "Services cloud.", Categorie.SCIENCE,
                                                        Arrays.asList("cloud", "informatique"), "FR", "Cloud Journal",
                                                        3, 2, "15–35", "10.6666/cj.2023.3.2"),
                                        new ArticleUniversitaire(50, "Robotique Avancée", Arrays.asList("Dr. G"), 2024,
                                                        "Robotics J.", "Robotique moderne.", Categorie.SCIENCE,
                                                        Arrays.asList("robotique", "IA"), "FR", "Robotics Journal", 6,
                                                        3, "101–130", "10.7777/rj.2024.6.3")
                        };
                        Document[] theses = {
                                        new These(51, "Algorithmes Quantiques", Arrays.asList("Martin Dupont"), 2023,
                                                        "Université de Paris", "Thèse sur l'informatique quantique.",
                                                        Categorie.SCIENCE, Arrays.asList("quantique", "algorithme"),
                                                        "FR", "Martin Dupont", "Pr. Lafontaine",
                                                        "Université de Paris-Saclay", "Informatique Théorique",
                                                        LocalDate.of(2023, 6, 15), "Accès Libre"),
                                        new These(52, "Matériaux Nanostructurés", Arrays.asList("Claire Morel"), 2022,
                                                        "Université de Lyon", "Thèse sur les nanomatériaux.",
                                                        Categorie.SCIENCE, Arrays.asList("nano", "physique"), "FR",
                                                        "Claire Morel", "Pr. Junot", "Université de Lyon",
                                                        "Physique des Matériaux", LocalDate.of(2022, 11, 7),
                                                        "Restreint"),
                                        new These(53, "Climatologie Avancée", Arrays.asList("Ahmed Bakri"), 2024,
                                                        "Université de Toulouse", "Modélisation climatique.",
                                                        Categorie.SCIENCE, Arrays.asList("climat", "modélisation"),
                                                        "FR", "Ahmed Bakri", "Pr. Valette", "Université de Toulouse",
                                                        "Climatologie", LocalDate.of(2024, 3, 21), "Accès Libre"),
                                        new These(54, "Énergies Renouvelables", Arrays.asList("Laura Petit"), 2023,
                                                        "Université Grenoble Alpes",
                                                        "Thèse sur le solaire et l’éolien.", Categorie.SCIENCE,
                                                        Arrays.asList("énergie", "solaire"), "FR", "Laura Petit",
                                                        "Pr. Martin", "Université Grenoble Alpes", "Énergies",
                                                        LocalDate.of(2023, 5, 10), "Accès Libre"),
                                        new These(55, "Intelligence Artificielle", Arrays.asList("Jean Bernard"), 2024,
                                                        "Université Paris", "IA avancée.", Categorie.SCIENCE,
                                                        Arrays.asList("IA", "apprentissage"), "FR", "Jean Bernard",
                                                        "Pr. Lefevre", "Université Paris", "Informatique",
                                                        LocalDate.of(2024, 2, 20), "Restreint"),
                                        new These(56, "Physique des Particules", Arrays.asList("Sophie Laurent"), 2022,
                                                        "Université de Lyon", "Thèse sur le boson de Higgs.",
                                                        Categorie.SCIENCE, Arrays.asList("physique", "particule"), "FR",
                                                        "Sophie Laurent", "Pr. Durand", "Université de Lyon",
                                                        "Physique", LocalDate.of(2022, 9, 18), "Accès Libre"),
                                        new These(57, "Biotechnologies", Arrays.asList("Nina Dubois"), 2023,
                                                        "Université Paris-Saclay", "Thèse sur CRISPR.",
                                                        Categorie.SCIENCE, Arrays.asList("biotech", "CRISPR"), "FR",
                                                        "Nina Dubois", "Pr. Rousseau", "Université Paris-Saclay",
                                                        "Biologie", LocalDate.of(2023, 4, 12), "Restreint"),
                                        new These(58, "Robotique Avancée", Arrays.asList("Alexandre Petit"), 2024,
                                                        "Université Toulouse", "Thèse sur robots autonomes.",
                                                        Categorie.SCIENCE, Arrays.asList("robotique", "IA"), "FR",
                                                        "Alexandre Petit", "Pr. Valette", "Université Toulouse",
                                                        "Robotique", LocalDate.of(2024, 6, 1), "Accès Libre"),
                                        new These(59, "Systèmes Distribués", Arrays.asList("Clara Martin"), 2023,
                                                        "Université Grenoble Alpes", "Thèse sur cloud computing.",
                                                        Categorie.SCIENCE, Arrays.asList("cloud", "informatique"), "FR",
                                                        "Clara Martin", "Pr. Martin", "Université Grenoble Alpes",
                                                        "Informatique", LocalDate.of(2023, 11, 5), "Restreint"),
                                        new These(60, "Écologie Urbaine", Arrays.asList("Paul Durand"), 2024,
                                                        "Université Paris", "Thèse sur villes durables.",
                                                        Categorie.SCIENCE, Arrays.asList("écologie", "ville"), "FR",
                                                        "Paul Durand", "Pr. Lefevre", "Université Paris",
                                                        "Environnement", LocalDate.of(2024, 3, 10), "Accès Libre")
                        };

                        for (Document l : livres) {
                                documentDAO.insert(l);
                        }
                        for (Document m : magazines) {
                                documentDAO.insert(m);
                        }
                        for (Document d : dvds) {
                                documentDAO.insert(d);
                        }
                        for (Document eb : ebooks) {
                                documentDAO.insert(eb);
                        }
                        for (Document a : articles) {
                                documentDAO.insert(a);
                        }
                        for (Document t : theses)
                                documentDAO.insert(t);

                        System.out.println("✔ Documents insérés avec succès !");
                        conn.commit();

                } catch (Exception e) {
                        System.err.println("Erreur seed :" + e.getMessage());
                }
        }

        // ====================================================================
        // INSERTION DES ADHERENTS (SI VIDE)
        // ====================================================================
        private static void seedAdherents() {
                try (Connection conn = getConnection()) {
                        conn.setAutoCommit(false);
                        AdherentDAO adherentDAO = new AdherentDAO(new EmpruntDAO(),
                                        new DocumentDAO());

                        Adherent[] adherents = {
                                        new Adherent(0, "Martin", "Lucas", "lucas.martin@mail.com",
                                                        "12 Rue des Lilas", "0612345678", LocalDate.of(2022, 1, 15),
                                                        StatutAdherent.ACTIF, null),
                                        new Adherent(0, "Dubois", "Emma", "emma.dubois@mail.com",
                                                        "8 Avenue Victor Hugo", "0698765432", LocalDate.of(2021, 5, 20),
                                                        StatutAdherent.ACTIF, null),
                                        new Adherent(0, "Durand", "Hugo", "hugo.durand@mail.com",
                                                        "5 Rue de la Paix", "0678123456", LocalDate.of(2023, 3, 10),
                                                        StatutAdherent.SUSPENDU, null),
                                        new Adherent(0, "Bernard", "Chloé", "chloe.bernard@mail.com",
                                                        "102 Boulevard Voltaire", "0611223344",
                                                        LocalDate.of(2020, 11, 1), StatutAdherent.ACTIF, null),
                                        new Adherent(0, "Petit", "Arthur", "arthur.petit@mail.com",
                                                        "23 Rue Nationale", "0655443322", LocalDate.of(2022, 8, 5),
                                                        StatutAdherent.SUSPENDU, null)
                        };

                        for (Adherent a : adherents) {
                                adherentDAO.save(a); // insère chaque adhérent
                        }
                        conn.commit();
                        System.out.println(" Adhérents insérés avec succès !");
                } catch (Exception e) {
                        System.err.println("Erreur seed adhérents : " + e.getMessage());
                }
        }

        // ====================================================================
        public static void main(String[] args) {

                initialize();
                seedDatabase();
                seedAdherents();

        }
}
