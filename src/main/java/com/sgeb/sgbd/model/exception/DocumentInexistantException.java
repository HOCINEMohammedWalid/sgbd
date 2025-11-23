package com.sgeb.sgbd.model.exception;

public class DocumentInexistantException {
    public DocumentInexistantException(int id) {
        super("Le document avec ID " + id + " n'existe pas.");
    }
}
