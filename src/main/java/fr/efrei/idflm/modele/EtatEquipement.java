package fr.efrei.idflm.modele;

public enum EtatEquipement {
    EN_SERVICE("En service"),
    EN_PANNE("En panne"),
    HORS_SERVICE("Hors service");

    private final String libelle;

    EtatEquipement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
