package fr.efrei.idflm.exception;

public class PrestataireIntrouvableException extends RessourceIntrouvableException {

    public PrestataireIntrouvableException(Integer id) {
        super("Le prestataire " + id + " n'existe pas");
    }
}
