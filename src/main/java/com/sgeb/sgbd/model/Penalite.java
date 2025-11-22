package com.sgeb.sgbd.model;

import java.time.LocalDate;

public class Penalite {
    private double montant;
    private String raison;
    private boolean payee;
    private LocalDate date;

    public Penalite(double montant, String raison, LocalDate date) {
        this.montant = montant;
        this.raison = raison;
        this.date = date != null ? date : LocalDate.now();
        this.payee = false;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getRaison() {
        return raison;
    }

    public void setRaison(String raison) {
        this.raison = raison;
    }

    public boolean isPayee() {
        return payee;
    }

    public void setPayee(boolean payee) {
        this.payee = payee;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("Penalite[%.2f€, %s, %s, payée=%s]", montant, raison, date, payee);
    }
}
