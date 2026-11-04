package fr.efrei.idflm.exception;

public class EquipementIntrouvableException extends RessourceIntrouvableException {

    public EquipementIntrouvableException(Integer id) {
        super("L'équipement " + id + " n'existe pas");
    }
}
