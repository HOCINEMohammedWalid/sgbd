package com.sgeb.sgbd.model.exception;

public class DocumentInexistantException extends Exception {
    public DocumentInexistantException(int id) {
        super("Le document avec ID " + id + " n'existe pas.");
    }
}
