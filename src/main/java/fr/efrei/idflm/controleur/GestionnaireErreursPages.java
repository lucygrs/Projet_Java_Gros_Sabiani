package fr.efrei.idflm.controleur;

import fr.efrei.idflm.exception.ExceptionMetier;
import fr.efrei.idflm.exception.MotDePasseObligatoireException;
import fr.efrei.idflm.exception.RessourceIntrouvableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice(basePackages = "fr.efrei.idflm.controleur")
public class GestionnaireErreursPages {

    private static final Logger journal = LoggerFactory.getLogger(GestionnaireErreursPages.class);
    private static final String ENTETE_REQUETE_FETCH = "X-Requested-With";
    private static final String VALEUR_REQUETE_FETCH = "fetch";

    @ExceptionHandler(RessourceIntrouvableException.class)
    public String ressourceIntrouvable(RessourceIntrouvableException exception, HttpServletRequest requete,
                                       HttpServletResponse reponse, Model modele) {
        return afficherErreur(HttpStatus.NOT_FOUND, exception.getMessage(), requete, reponse, modele);
    }

    @ExceptionHandler({MotDePasseObligatoireException.class, MethodArgumentTypeMismatchException.class})
    public String donneeInvalide(Exception exception, HttpServletRequest requete, HttpServletResponse reponse,
                                 Model modele) {
        String message = exception instanceof ExceptionMetier ? exception.getMessage() : "La requête est invalide";
        return afficherErreur(HttpStatus.BAD_REQUEST, message, requete, reponse, modele);
    }

    @ExceptionHandler(ExceptionMetier.class)
    public String conflit(ExceptionMetier exception, HttpServletRequest requete, HttpServletResponse reponse,
                          Model modele) {
        return afficherErreur(HttpStatus.CONFLICT, exception.getMessage(), requete, reponse, modele);
    }

    // Frontière de l'application : on journalise la trace et on n'affiche qu'un message générique
    @ExceptionHandler(Exception.class)
    public String erreurInattendue(Exception exception, HttpServletRequest requete, HttpServletResponse reponse,
                                   Model modele) {
        journal.error("Erreur inattendue sur {}", requete.getRequestURI(), exception);
        return afficherErreur(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur inattendue est survenue, veuillez réessayer plus tard", requete, reponse, modele);
    }

    private String afficherErreur(HttpStatus statut, String message, HttpServletRequest requete,
                                  HttpServletResponse reponse, Model modele) {
        reponse.setStatus(statut.value());
        modele.addAttribute("status", statut.value());
        modele.addAttribute("message", message);
        boolean depuisPopup = VALEUR_REQUETE_FETCH.equals(requete.getHeader(ENTETE_REQUETE_FETCH));
        return depuisPopup ? "popups/message" : "error";
    }
}
