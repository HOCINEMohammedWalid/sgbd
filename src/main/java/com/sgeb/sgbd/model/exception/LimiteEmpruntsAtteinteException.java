package com.sgeb.sgbd.model.exception;

public class LimiteEmpruntsAtteinteException extends EmpruntException {
    public LimiteEmpruntsAtteinteException() {
        super("L'adhérent a atteint le nombre maximal d'emprunts.");
    }
}
