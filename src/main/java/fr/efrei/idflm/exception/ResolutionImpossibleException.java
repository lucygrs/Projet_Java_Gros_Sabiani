package fr.efrei.idflm.exception;

public class ResolutionImpossibleException extends ExceptionMetier {

    public ResolutionImpossibleException(Integer idIncident, String raison) {
        super("L'incident " + idIncident + " ne peut pas être résolu : " + raison);
    }
}
