package com.sgeb.sgbd.model.exception;
public class AdherentExistantException extends AdherentException {
    public AdherentExistantException(int id) {
        super("L'adhérent avec l'ID " + id + " existe déjà.");
    }
}

