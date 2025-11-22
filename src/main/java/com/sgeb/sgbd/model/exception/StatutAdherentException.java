package com.sgeb.sgbd.model.exception;


public class StatutAdherentException extends EmpruntException {
    public StatutAdherentException() {
        super("L'adhérent ne peut pas emprunter (statut ou pénalités).");
 
    }}