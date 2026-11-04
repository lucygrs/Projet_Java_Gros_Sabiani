package fr.efrei.idflm.exception;

public class TransitionInterditeException extends ExceptionMetier {

    public TransitionInterditeException(Integer idIncident, String operationRefusee) {
        super("L'incident " + idIncident + " est déjà résolu : il ne peut plus être " + operationRefusee);
    }
}
