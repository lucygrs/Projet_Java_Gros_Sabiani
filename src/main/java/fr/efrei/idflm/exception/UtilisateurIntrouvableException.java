package fr.efrei.idflm.exception;

public class UtilisateurIntrouvableException extends RessourceIntrouvableException {

    public UtilisateurIntrouvableException(Integer id) {
        super("L'utilisateur " + id + " n'existe pas");
    }
}
