package com.sgeb.sgbd.model.exception;

public class DocumentExistantException extends Exception {
    public DocumentExistantException(int id) {
        super("Le document avec ID " + id + " existe déjà.");
    }
}
