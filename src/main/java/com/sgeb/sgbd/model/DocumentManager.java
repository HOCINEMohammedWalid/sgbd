package com.sgeb.sgbd.model;

import com.sgeb.sgbd.dao.DocumentDAO;
import com.sgeb.sgbd.model.enums.Categorie;
import com.sgeb.sgbd.model.exception.*;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DocumentManager {

    private final Map<Integer, Document> catalogue = new HashMap<>();
    private final DocumentDAO dao;

    public DocumentManager(DocumentDAO dao) throws SQLException {
        this.dao = dao;
        chargerDepuisBDD();
    }

    /** Charge tous les documents depuis la base dans le catalogue en mémoire */
    public void chargerDepuisBDD() throws SQLException {
        List<Document> docs = dao.findAll();
        catalogue.clear();
        for (Document d : docs) {
            catalogue.put(d.getIdDocument(), d);
        }
    }

    /** Ajouter un document en mémoire et en base */
    public void ajouterDocument(Document doc) throws SQLException, DocumentExistantException {
        if (doc == null)
            throw new IllegalArgumentException("Document invalide");
        if (catalogue.containsKey(doc.getIdDocument()))
            throw new DocumentExistantException(doc.getIdDocument());

        dao.insert(doc); // récupération automatique de l'ID si AUTOINCREMENT
        catalogue.put(doc.getIdDocument(), doc);
    }

    /** Supprimer un document de la base et du catalogue */
    public void supprimerDocument(int id) throws SQLException, DocumentInexistantException {
        Document doc = catalogue.get(id);
        if (doc == null)
            throw new DocumentInexistantException(id);

        dao.delete(doc);
        catalogue.remove(id);
    }

    /** Modifier un document et synchroniser la base */
    public boolean modifierDocument(int id, java.util.function.Consumer<Document> modification)
            throws SQLException, DocumentInexistantException {
        Document doc = catalogue.get(id);
        if (doc == null)
            throw new DocumentInexistantException(id);
        if (modification != null) {
            modification.accept(doc);
            dao.update(doc);
            return true;
        }
        return false;
    }

    public boolean modifierTitre(int id, String nouveauTitre) throws SQLException, DocumentInexistantException {
        return modifierDocument(id, d -> d.setTitre(nouveauTitre));
    }

    public boolean modifierAuteurs(int id, List<String> nouveauxAuteurs)
            throws SQLException, DocumentInexistantException {
        return modifierDocument(id, d -> d.setAuteurs(nouveauxAuteurs));
    }

    // -------------------- Recherches --------------------
    private List<Document> rechercher(Predicate<Document> critere) {
        return catalogue.values().stream()
                .filter(critere)
                .collect(Collectors.toList());
    }

    public Optional<Document> rechercherParId(int id) {
        return Optional.ofNullable(catalogue.get(id));
    }

    public List<Document> rechercherParTitre(String titre) {
        return rechercher(d -> d.getTitre() != null &&
                d.getTitre().toLowerCase().contains(titre.toLowerCase()));
    }

    public List<Document> rechercherParAuteur(String auteur) {
        return rechercher(d -> d.getAuteurs() != null &&
                d.getAuteurs().stream().anyMatch(a -> a.equalsIgnoreCase(auteur)));
    }

    public List<Document> rechercherParCategorie(Categorie categorie) {
        return rechercher(d -> categorie.equals(d.getCategorie()));
    }

    public List<Document> rechercherMultiCritere(Predicate<Document> critere) {
        return rechercher(critere);
    }

    // -------------------- Listes --------------------
    public List<Document> listerTousDocuments() {
        return catalogue.values().stream()
                .sorted(Comparator.comparing(Document::getTitre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<Document> listerFiltreEtTrie(Predicate<Document> filtre, Comparator<Document> comparateur) {
        return catalogue.values().stream()
                .filter(filtre)
                .sorted(comparateur)
                .collect(Collectors.toList());
    }

    public int tailleCatalogue() {
        return catalogue.size();
    }

    /** Recharge tout le catalogue depuis la base */
    public void rechargerTout() throws SQLException {
        catalogue.clear();
        chargerDepuisBDD();
    }

    public static void main(String[] args) {
        try {
            DocumentDAO dao = new DocumentDAO();
            DocumentManager manager = new DocumentManager(dao);

            System.out.println("Catalogue initial : " + manager.tailleCatalogue());

            // -------------------- AJOUT --------------------
            Livre livre = new Livre(
                    400, // ID = 0 pour autoincrement
                    "Test Livre",
                    Arrays.asList("Auteur Test"),
                    2025,
                    "Editeur Test",
                    "Résumé du test",
                    Categorie.LITTERATURE,
                    Arrays.asList("test", "java"),
                    "FR",
                    "1234567890",
                    200,
                    "Collection Test");

            manager.ajouterDocument(livre);
            System.out.println("Après ajout : " + manager.tailleCatalogue());

            // -------------------- RECHERCHE --------------------
            List<Document> resultTitre = manager.rechercherParTitre("Test Livre");
            System.out.println("Recherche par titre : " + resultTitre.size() + " résultat(s)");

            List<Document> resultAuteur = manager.rechercherParAuteur("Auteur Test");
            System.out.println("Recherche par auteur : " + resultAuteur.size() + " résultat(s)");
            List<Document> result = manager.rechercher(a -> "5".equals(a.getLangue()));
            System.out.println("Recherche par langue : " + result.size() + " résultat(s)");

            System.out.println("Recherche par auteur : " + resultAuteur.size() + " résultat(s)");

            // -------------------- MODIFICATION --------------------
            manager.modifierTitre(livre.getIdDocument(), "Titre Modifié");
            Document modifie = manager.rechercherParId(livre.getIdDocument()).orElse(null);
            System.out.println("Titre après modification : " + modifie.getTitre());

            // -------------------- SUPPRESSION --------------------
            manager.supprimerDocument(livre.getIdDocument());
            System.out.println("Après suppression : " + manager.tailleCatalogue());

        } catch (SQLException | DocumentExistantException | DocumentInexistantException e) {
            e.printStackTrace();
        }
    }
}
