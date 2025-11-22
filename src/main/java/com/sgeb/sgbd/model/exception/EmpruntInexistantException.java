package com.sgeb.sgbd.model.exception;



public class EmpruntInexistantException extends EmpruntException {
    public EmpruntInexistantException() {
        super("L'emprunt spécifié n'existe pas.");
    }
}