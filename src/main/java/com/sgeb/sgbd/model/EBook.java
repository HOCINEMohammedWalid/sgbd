package com.sgeb.sgbd.model;

import com.sgeb.sgbd.model.enums.TypeDocument;

public class EBook extends Document {
    private String urlAcces;
    private String format;
    private boolean drm;

    public EBook(int idDocument, String titre, String urlAcces, String format, boolean drm) {
        super(idDocument, titre);
        this.urlAcces = urlAcces;
        this.format = format;
        this.drm = drm;
        this.typeDocument = TypeDocument.EBOOK;
    }

    // Getters et setters
    public String getUrlAcces() {
        return urlAcces;
    }

    public void setUrlAcces(String urlAcces) {
        this.urlAcces = urlAcces;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean hasDrm() {
        return drm;
    }

    public void setDrm(boolean drm) {
        this.drm = drm;
    }

    // Méthodes abstraites de Document
    @Override
    public String getDescriptionComplete() {
        return String.format("EBook[id=%d, titre='%s', url='%s', format='%s', DRM=%b]",
                idDocument, titre, urlAcces, format, drm);
    }

    @Override
    public void afficherResume() {
        System.out.println("Résumé de l'EBook : " + super.resume);
    }

    @Override
    public String toString() {
        return getDescriptionComplete();
    }
}
