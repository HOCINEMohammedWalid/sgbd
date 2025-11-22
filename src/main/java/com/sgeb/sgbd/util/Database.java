package com.sgeb.sgbd.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe Database : gère la connexion et l'initialisation/mise à jour des
 * tables SQLite
 * avec support de l'héritage et des contraintes ON DELETE CASCADE.
 */
public class Database {

    private static final String URL = "jdbc:sqlite:sgbd.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Initialise toutes les tables si elles n'existent pas déjà.
     */
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
                            "statut TEXT NOT NULL" +
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
                            "langue TEXT" +
                            ");");

            // Tables spécialisées avec ON DELETE CASCADE
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

            // ------------------ Table Emprunt ------------------
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS emprunt (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "document_id INTEGER NOT NULL," +
                            "adherent_id INTEGER NOT NULL," +
                            "date_emprunt TEXT NOT NULL," +
                            "date_retour_prevue TEXT NOT NULL," +
                            "date_retour_reelle TEXT," +
                            "penalite REAL," +
                            "FOREIGN KEY(document_id) REFERENCES document(id) ON DELETE CASCADE," +
                            "FOREIGN KEY(adherent_id) REFERENCES adherent(id) ON DELETE CASCADE" +
                            ");");

            // ------------------ Table Penalite ------------------
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS penalite (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "adherent_id INTEGER NOT NULL," +
                            "montant REAL NOT NULL," +
                            "payee INTEGER NOT NULL," +
                            "motif TEXT," +
                            "FOREIGN KEY(adherent_id) REFERENCES adherent(id) ON DELETE CASCADE" +
                            ");");

            System.out.println("Tables initialisées avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation des tables : " + e.getMessage());
        }
    }

    /**
     * Met à jour le schéma pour appliquer ON DELETE CASCADE et autres modifications
     * sur les tables existantes.
     */
    public static void updateSchema() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("PRAGMA foreign_keys = OFF;");

            // Supprimer les tables spécialisées si elles existent
            stmt.executeUpdate("DROP TABLE IF EXISTS livre;");
            stmt.executeUpdate("DROP TABLE IF EXISTS magazine;");
            stmt.executeUpdate("DROP TABLE IF EXISTS dvd;");
            stmt.executeUpdate("DROP TABLE IF EXISTS ebook;");
            stmt.executeUpdate("DROP TABLE IF EXISTS article_universitaire;");
            stmt.executeUpdate("DROP TABLE IF EXISTS these;");

            // Supprimer les tables dépendantes
            stmt.executeUpdate("DROP TABLE IF EXISTS emprunt;");
            stmt.executeUpdate("DROP TABLE IF EXISTS penalite;");

            // Supprimer la table de base
            stmt.executeUpdate("DROP TABLE IF EXISTS document;");
            stmt.executeUpdate("DROP TABLE IF EXISTS adherent;");

            // Réinitialiser les tables avec ON DELETE CASCADE
            initialize(); // ta méthode initialize() doit être modifiée pour inclure ON DELETE CASCADE sur
                          // toutes les clés étrangères

            stmt.executeUpdate("PRAGMA foreign_keys = ON;");
            System.out.println("Schéma mis à jour avec ON DELETE CASCADE !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du schéma : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initialize();
        updateSchema();
    }
}
