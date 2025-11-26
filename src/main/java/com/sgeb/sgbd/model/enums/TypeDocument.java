package com.sgeb.sgbd.model.enums;

public enum TypeDocument {
    LIVRE("/com/sgeb/sgbd/view/DetailsLivre.fxml", "/com/sgeb/sgbd/view/EmpruntLivre.fxml"),
    MAGAZINE("/com/sgeb/sgbd/view/DetailsMagazine.fxml", "/com/sgeb/sgbd/view/EmpruntMagazine.fxml"),
    DVD("/com/sgeb/sgbd/view/DetailsDVD.fxml", "/com/sgeb/sgbd/view/EmpruntDVD.fxml"),
    EBOOK("/com/sgeb/sgbd/view/DetailsEbook.fxml", "/com/sgeb/sgbd/view/EmpruntEbook.fxml"),
    THESE("/com/sgeb/sgbd/view/DetailsThese.fxml", "/com/sgeb/sgbd/view/EmpruntThese.fxml"),
    ARTICLE("/com/sgeb/sgbd/view/DetailsArticleUniv.fxml", "/com/sgeb/sgbd/view/EmpruntArticleUniv.fxml");

    private final String fxmlPath;
    private final String fxmlPathNonAdmin;

    TypeDocument(String fxmlPath, String fxmlPathNonAdmin) {
        this.fxmlPath = fxmlPath;
        this.fxmlPathNonAdmin = fxmlPathNonAdmin;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }

    public String getFxmlPathNonAdmin() {
        return fxmlPathNonAdmin;
    }
}
