package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.enums.Categorie;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DocumentManager {

    private final Map<Integer, Document> catalogue = new HashMap<>();

    public boolean ajouterDocument(Document document) {
        if (document == null || catalogue.containsKey(document.getIdDocument()))
            return false;
        catalogue.put(document.getIdDocument(), document);
        return true;
    }

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

    // Recherche par auteur
    public List<Document> rechercherParAuteur(String auteur) {
        return rechercher(d -> d.getAuteurs() != null &&
                d.getAuteurs().stream().anyMatch(a -> a.equalsIgnoreCase(auteur)));
    }

    // Recherche par catégorie
    public List<Document> rechercherParCategorie(Categorie categorie) {
        return rechercher(d -> categorie.equals(d.getCategorie()));
    }

    // Recherche multi-critères avec un Predicate
    public List<Document> rechercherMultiCritere(Predicate<Document> critere) {
        return rechercher(critere);
    }

    // Appliquer une modification via un Consumer
    public boolean modifierDocument(int id, java.util.function.Consumer<Document> modification) {
        Document doc = catalogue.get(id);
        if (doc == null || modification == null)
            return false;
        modification.accept(doc);
        return true;
    }

    // Exemple : modifier uniquement le titre
    public boolean modifierTitre(int id, String nouveauTitre) {
        return modifierDocument(id, d -> d.setTitre(nouveauTitre));
    }

    public boolean supprimerDocument(int id) {
        return catalogue.remove(id) != null;
    }

    public List<Document> listerTousDocuments() {
        return catalogue.values().stream()
                .sorted(Comparator.comparing(Document::getTitre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    // Lister avec un filtre et un tri
    public List<Document> listerFiltreEtTrie(Predicate<Document> filtre, Comparator<Document> comparateur) {
        return catalogue.values().stream()
                .filter(filtre)
                .sorted(comparateur)
                .collect(Collectors.toList());
    }

    public int tailleCatalogue() {
        return catalogue.size();
    }
}
