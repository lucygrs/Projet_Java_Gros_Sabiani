package fr.efrei.idflm.controleur;

import fr.efrei.idflm.dto.UtilisateurFormulaire;
import fr.efrei.idflm.modele.Profil;
import fr.efrei.idflm.modele.Utilisateur;
import fr.efrei.idflm.service.UtilisateurService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/utilisateurs")
public class UtilisateurControleur {

    private static final String REDIRECTION_UTILISATEURS = "redirect:/utilisateurs";
    private static final String POPUP_FORMULAIRE = "popups/formulaire-utilisateur";

    private final UtilisateurService utilisateurService;

    public UtilisateurControleur(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public String afficherUtilisateurs(@RequestParam(defaultValue = "") String recherche,
                                       @RequestParam(defaultValue = "0") int page,
                                       HttpServletRequest requete, Model modele) {
        Page<Utilisateur> utilisateurs = utilisateurService.rechercherUtilisateurs(
                recherche, Pagination.demanderPage(page));
        modele.addAttribute("utilisateurs", utilisateurs.getContent());
        modele.addAttribute("pagination", Pagination.decrire(utilisateurs, requete));
        modele.addAttribute("recherche", recherche);
        return "utilisateurs";
    }

    @GetMapping("/nouveau")
    public String afficherCreation(Model modele) {
        modele.addAttribute("formulaire", new UtilisateurFormulaire("", "", "", "", Profil.AGENT_SIGNALEMENT));
        return preparerFormulaire(null, modele);
    }

    @PostMapping
    public String creerUtilisateur(@Valid @ModelAttribute("formulaire") UtilisateurFormulaire formulaire,
                                   BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            return preparerFormulaire(null, modele);
        }
        utilisateurService.creerUtilisateur(formulaire);
        return REDIRECTION_UTILISATEURS;
    }

    @GetMapping("/{idUtilisateur}/modification")
    public String afficherModification(@PathVariable Integer idUtilisateur, Model modele) {
        Utilisateur utilisateur = utilisateurService.trouverUtilisateur(idUtilisateur);
        modele.addAttribute("formulaire", new UtilisateurFormulaire(utilisateur.getIdentifiant(),
                utilisateur.getNom(), utilisateur.getPrenom(), "", utilisateur.getProfil()));
        return preparerFormulaire(idUtilisateur, modele);
    }

    @PutMapping("/{idUtilisateur}")
    public String modifierUtilisateur(@PathVariable Integer idUtilisateur,
                                      @Valid @ModelAttribute("formulaire") UtilisateurFormulaire formulaire,
                                      BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            return preparerFormulaire(idUtilisateur, modele);
        }
        utilisateurService.modifierUtilisateur(idUtilisateur, formulaire);
        return REDIRECTION_UTILISATEURS;
    }

    @DeleteMapping("/{idUtilisateur}")
    public String supprimerUtilisateur(@PathVariable Integer idUtilisateur, Authentication authentification) {
        utilisateurService.supprimerUtilisateur(idUtilisateur, authentification.getName());
        return REDIRECTION_UTILISATEURS;
    }

    private String preparerFormulaire(Integer idUtilisateur, Model modele) {
        modele.addAttribute("idUtilisateur", idUtilisateur);
        modele.addAttribute("profils", Profil.values());
        return POPUP_FORMULAIRE;
    }
}
