package fr.efrei.idflm.controleur;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ConnexionControleur {

    @GetMapping("/connexion")
    public String afficherConnexion() {
        return "connexion";
    }

    @GetMapping("/")
    public String afficherAccueil(Model modele) {
        boolean estResponsable = Boolean.TRUE.equals(modele.getAttribute("estResponsable"));
        return estResponsable ? "redirect:/tableau-de-bord" : "redirect:/incidents";
    }

    @GetMapping("/acces-refuse")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String afficherAccesRefuse(Model modele) {
        modele.addAttribute("status", HttpStatus.FORBIDDEN.value());
        modele.addAttribute("message", "Votre profil ne vous permet pas d'accéder à cette page");
        return "error";
    }
}
