package com.sgeb.sgbd.model.exception;

public class AdherentInexistantException extends AdherentException {
    public AdherentInexistantException(int id) {
        super("Aucun adhérent trouvé avec l'ID " + id + ".");
    }
}
