package fr.efrei.idflm.exception;

public class StationIntrouvableException extends RessourceIntrouvableException {

    public StationIntrouvableException(Integer id) {
        super("La station " + id + " n'existe pas");
    }
}
