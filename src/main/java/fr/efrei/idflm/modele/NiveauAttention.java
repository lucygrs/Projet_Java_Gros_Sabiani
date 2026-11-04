package fr.efrei.idflm.modele;

public enum NiveauAttention {
    VERT("Ouvert depuis moins de 48 h"),
    ORANGE("Ouvert depuis 48 h à moins de 72 h"),
    ROUGE("Ouvert depuis 72 h ou plus"),
    PRIS_EN_CHARGE("Pris en charge par un prestataire");

    private final String libelle;

    NiveauAttention(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
