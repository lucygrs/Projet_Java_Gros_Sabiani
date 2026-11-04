package fr.efrei.idflm.controleur;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

public final class ErreursFormulaire {

    private ErreursFormulaire() {
    }

    // La pop-up est renvoyée avec le code 400 pour que le script la réaffiche avec les messages
    public static void signaler(BindingResult resultat, HttpServletResponse reponse, Model modele) {
        reponse.setStatus(HttpStatus.BAD_REQUEST.value());
        modele.addAttribute("erreurs", resultat.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList());
    }
}
