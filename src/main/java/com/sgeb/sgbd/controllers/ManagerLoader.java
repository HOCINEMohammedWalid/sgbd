// Créez ce nouveau fichier : ManagerLoader.java (dans le package controllers ou utils)
package com.sgeb.sgbd.controllers; // Ou com.sgeb.sgbd.utils

import com.sgeb.sgbd.model.Adherent;
import com.sgeb.sgbd.model.AdherentManager;
import com.sgeb.sgbd.model.DocumentManager;
import com.sgeb.sgbd.model.EmpruntManager;

public interface ManagerLoader {
    void setManagers(DocumentManager docM, AdherentManager adhM, EmpruntManager empM);

    default void setAdherent(Adherent adherent) {
    };
}
