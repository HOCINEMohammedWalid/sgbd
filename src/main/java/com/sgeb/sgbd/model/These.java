package com.sgeb.sgbd.model;

import com.sgeb.sgbd.enums.TypeDocument;
import java.time.LocalDate;

public class These extends Document {
    private String auteurPrincipal;
    private String directeurRecherche;
    private String universite;
    private String discipline;
    private LocalDate dateSoutenance;
    private String typeAcces; // public, restreint

    public These(int idDocument, String titre, String auteurPrincipal, String universite, LocalDate dateSoutenance) {
        super(idDocument, titre);
        this.auteurPrincipal = auteurPrincipal;
        this.universite = universite;
        this.dateSoutenance = dateSoutenance;
        this.typeDocument = TypeDocument.THESE;
    }
}
