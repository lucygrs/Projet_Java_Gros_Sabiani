package fr.efrei.idflm.exception;

public class LigneIntrouvableException extends RessourceIntrouvableException {

    public LigneIntrouvableException(Integer id) {
        super("La ligne " + id + " n'existe pas");
    }
}
