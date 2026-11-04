package fr.efrei.idflm.exception;

public class SuppressionImpossibleException extends ExceptionMetier {

    public SuppressionImpossibleException(String raison) {
        super("Suppression impossible : " + raison);
    }
}
