package fr.efrei.idflm.controleur;

import fr.efrei.idflm.dto.PrestataireFormulaire;
import fr.efrei.idflm.modele.Prestataire;
import fr.efrei.idflm.service.PrestataireService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
@RequestMapping("/prestataires")
public class PrestataireControleur {

    private static final String REDIRECTION_PRESTATAIRES = "redirect:/prestataires";
    private static final String POPUP_FORMULAIRE = "popups/formulaire-prestataire";

    private final PrestataireService prestataireService;

    public PrestataireControleur(PrestataireService prestataireService) {
        this.prestataireService = prestataireService;
    }

    @GetMapping
    public String afficherPrestataires(@RequestParam(defaultValue = "") String recherche,
                                       @RequestParam(defaultValue = "0") int page,
                                       HttpServletRequest requete, Model modele) {
        Page<Prestataire> prestataires = prestataireService.rechercherPrestataires(
                recherche, Pagination.demanderPage(page));
        modele.addAttribute("prestataires", prestataires.getContent());
        modele.addAttribute("pagination", Pagination.decrire(prestataires, requete));
        modele.addAttribute("recherche", recherche);
        return "prestataires";
    }

    @GetMapping("/nouveau")
    public String afficherCreation(Model modele) {
        modele.addAttribute("formulaire", new PrestataireFormulaire("", ""));
        modele.addAttribute("idPrestataire", null);
        return POPUP_FORMULAIRE;
    }

    @PostMapping
    public String creerPrestataire(@Valid @ModelAttribute("formulaire") PrestataireFormulaire formulaire,
                                   BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            modele.addAttribute("idPrestataire", null);
            return POPUP_FORMULAIRE;
        }
        prestataireService.creerPrestataire(formulaire);
        return REDIRECTION_PRESTATAIRES;
    }

    @GetMapping("/{idPrestataire}/modification")
    public String afficherModification(@PathVariable Integer idPrestataire, Model modele) {
        Prestataire prestataire = prestataireService.trouverPrestataire(idPrestataire);
        modele.addAttribute("formulaire", new PrestataireFormulaire(prestataire.getNom(), prestataire.getContact()));
        modele.addAttribute("idPrestataire", idPrestataire);
        return POPUP_FORMULAIRE;
    }

    @PutMapping("/{idPrestataire}")
    public String modifierPrestataire(@PathVariable Integer idPrestataire,
                                      @Valid @ModelAttribute("formulaire") PrestataireFormulaire formulaire,
                                      BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            modele.addAttribute("idPrestataire", idPrestataire);
            return POPUP_FORMULAIRE;
        }
        prestataireService.modifierPrestataire(idPrestataire, formulaire);
        return REDIRECTION_PRESTATAIRES;
    }

    @DeleteMapping("/{idPrestataire}")
    public String supprimerPrestataire(@PathVariable Integer idPrestataire) {
        prestataireService.supprimerPrestataire(idPrestataire);
        return REDIRECTION_PRESTATAIRES;
    }
}
