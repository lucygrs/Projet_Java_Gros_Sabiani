package fr.efrei.idflm.api;

import fr.efrei.idflm.dto.ErreurApi;
import fr.efrei.idflm.exception.ExceptionMetier;
import fr.efrei.idflm.exception.MotDePasseObligatoireException;
import fr.efrei.idflm.exception.RessourceIntrouvableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice(basePackages = "fr.efrei.idflm.api")
public class GestionnaireErreursApi {

    private static final Logger journal = LoggerFactory.getLogger(GestionnaireErreursApi.class);

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurApi> ressourceIntrouvable(RessourceIntrouvableException exception) {
        return repondre(HttpStatus.NOT_FOUND, exception.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurApi> donneesInvalides(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return repondre(HttpStatus.BAD_REQUEST, "Les données envoyées sont invalides", details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErreurApi> requeteIllisible() {
        return repondre(HttpStatus.BAD_REQUEST, "La requête est mal formée", List.of());
    }

    @ExceptionHandler(MotDePasseObligatoireException.class)
    public ResponseEntity<ErreurApi> donneeManquante(MotDePasseObligatoireException exception) {
        return repondre(HttpStatus.BAD_REQUEST, exception.getMessage(), List.of());
    }

    @ExceptionHandler(ExceptionMetier.class)
    public ResponseEntity<ErreurApi> conflit(ExceptionMetier exception) {
        return repondre(HttpStatus.CONFLICT, exception.getMessage(), List.of());
    }

    // Frontière de l'API : on journalise la trace et on ne renvoie qu'un message générique
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurApi> erreurInattendue(Exception exception) {
        journal.error("Erreur inattendue dans l'API", exception);
        return repondre(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur inattendue est survenue, veuillez réessayer plus tard", List.of());
    }

    private ResponseEntity<ErreurApi> repondre(HttpStatus statut, String message, List<String> details) {
        return ResponseEntity.status(statut).body(new ErreurApi(statut.value(), message, details));
    }
}
