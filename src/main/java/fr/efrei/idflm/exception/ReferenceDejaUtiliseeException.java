package fr.efrei.idflm.exception;

public class ReferenceDejaUtiliseeException extends ExceptionMetier {

    public ReferenceDejaUtiliseeException(String reference) {
        super("La référence " + reference + " est déjà utilisée par un autre équipement");
    }
}
