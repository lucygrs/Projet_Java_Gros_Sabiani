package fr.efrei.idflm.exception;

public class MotDePasseObligatoireException extends ExceptionMetier {

    public MotDePasseObligatoireException() {
        super("Un mot de passe est obligatoire pour créer un compte");
    }
}
