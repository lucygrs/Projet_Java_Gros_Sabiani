package fr.efrei.idflm.controleur;

import fr.efrei.idflm.dto.IncidentSuivi;
import fr.efrei.idflm.modele.Profil;
import fr.efrei.idflm.modele.Utilisateur;
import fr.efrei.idflm.service.IncidentService;
import fr.efrei.idflm.service.UtilisateurService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

// Fournit à toutes les pages le menu latéral : utilisateur connecté et signalements en cours
@ControllerAdvice
public class GabaritPages {

    private final UtilisateurService utilisateurService;
    private final IncidentService incidentService;

    public GabaritPages(UtilisateurService utilisateurService, IncidentService incidentService) {
        this.utilisateurService = utilisateurService;
        this.incidentService = incidentService;
    }

    @ModelAttribute("utilisateurConnecte")
    public Utilisateur utilisateurConnecte(Authentication authentification) {
        return authentification == null ? null : utilisateurService.trouverParIdentifiant(authentification.getName());
    }

    @ModelAttribute("estResponsable")
    public boolean estResponsable(Authentication authentification) {
        return authentification != null && authentification.getAuthorities().stream()
                .anyMatch(droit -> droit.getAuthority().equals("ROLE_" + Profil.RESPONSABLE_EXPLOITATION.name()));
    }

    @ModelAttribute("signalementsEnCours")
    public List<IncidentSuivi> signalementsEnCours(Authentication authentification) {
        return authentification == null ? List.of() : incidentService.listerSignalementsEnCours();
    }
}
