package com.sgeb.sgbd.controllers;

import javax.print.Doc;

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.Document;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;

import javafx.scene.control.TableCell;

/**
 * Interface commune pour tous les contrôleurs de fenêtres de détails.
 * Permet de passer un Document à afficher dans la vue.
 */
public interface DetailsControllerBase {

    /**
     * Initialise le contrôleur avec le document à afficher.
     *
     * @param document le document à afficher
     */
    void setDocument(Document document);

    void setDocumentManager(DocumentManager documentManager);

    void setParent(DocumentsController table);

    default public void setEmpruntManager(EmpruntManager empruntManager) {

    }

    default public void setAdherent(Adherent adherent) {

    }

}
