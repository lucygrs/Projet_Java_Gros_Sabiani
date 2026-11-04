package fr.efrei.idflm.controleur;

import fr.efrei.idflm.dto.InformationsPersonnellesFormulaire;
import fr.efrei.idflm.dto.MotDePasseFormulaire;
import fr.efrei.idflm.modele.Utilisateur;
import fr.efrei.idflm.service.UtilisateurService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/parametres")
public class ParametresControleur {

    private static final String POPUP_PARAMETRES = "popups/parametres";

    private final UtilisateurService utilisateurService;
    private final SecurityContextRepository depotContexteSecurite = new HttpSessionSecurityContextRepository();

    public ParametresControleur(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public String afficherParametres() {
        return POPUP_PARAMETRES;
    }

    @PutMapping("/informations")
    public String modifierInformations(
            @Valid @ModelAttribute("informations") InformationsPersonnellesFormulaire formulaire,
            BindingResult resultat, Authentication authentification, HttpServletRequest requete,
            HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            return POPUP_PARAMETRES;
        }
        Utilisateur utilisateur = utilisateurService.modifierInformationsPersonnelles(
                authentification.getName(), formulaire);
        suivreNouvelIdentifiant(utilisateur.getIdentifiant(), authentification, requete, reponse);
        return "redirect:/";
    }

    @PutMapping("/mot-de-passe")
    public String changerMotDePasse(@Valid @ModelAttribute("formulaire") MotDePasseFormulaire formulaire,
                                    BindingResult resultat, Authentication authentification,
                                    HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            return POPUP_PARAMETRES;
        }
        utilisateurService.changerMotDePasse(authentification.getName(), formulaire);
        return "redirect:/";
    }

    // L'identifiant sert de nom de connexion : la session doit suivre le nouvel identifiant sans reconnexion
    private void suivreNouvelIdentifiant(String identifiant, Authentication authentification,
                                         HttpServletRequest requete, HttpServletResponse reponse) {
        SecurityContext contexte = SecurityContextHolder.createEmptyContext();
        contexte.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                identifiant, null, authentification.getAuthorities()));
        SecurityContextHolder.setContext(contexte);
        depotContexteSecurite.saveContext(contexte, requete, reponse);
    }
}
