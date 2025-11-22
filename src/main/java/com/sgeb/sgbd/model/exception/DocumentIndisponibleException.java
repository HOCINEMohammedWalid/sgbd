package com.sgeb.sgbd.model.exception;

public class DocumentIndisponibleException extends EmpruntException {
    public DocumentIndisponibleException() {
        super("Le document est déjà emprunté.");
    }
}
