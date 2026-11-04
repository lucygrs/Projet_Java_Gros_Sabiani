package fr.efrei.idflm.exception;

public class IdentifiantDejaUtiliseException extends ExceptionMetier {

    public IdentifiantDejaUtiliseException(String identifiant) {
        super("L'identifiant " + identifiant + " est déjà utilisé par un autre compte");
    }
}
