package fr.efrei.idflm.controleur;

import fr.efrei.idflm.dto.DeclarationIncidentFormulaire;
import fr.efrei.idflm.dto.IncidentSuivi;
import fr.efrei.idflm.dto.ModificationIncidentFormulaire;
import fr.efrei.idflm.dto.ResolutionFormulaire;
import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.service.EquipementService;
import fr.efrei.idflm.service.IncidentService;
import fr.efrei.idflm.service.PrestataireService;
import fr.efrei.idflm.service.StationService;
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

import java.util.List;

@Controller
@RequestMapping("/incidents")
public class IncidentControleur {

    private static final String REDIRECTION_INCIDENTS = "redirect:/incidents";

    private final IncidentService incidentService;
    private final EquipementService equipementService;
    private final PrestataireService prestataireService;
    private final StationService stationService;

    public IncidentControleur(IncidentService incidentService, EquipementService equipementService,
                              PrestataireService prestataireService, StationService stationService) {
        this.incidentService = incidentService;
        this.equipementService = equipementService;
        this.prestataireService = prestataireService;
        this.stationService = stationService;
    }

    @GetMapping
    public String afficherIncidentsEnCours(@RequestParam(defaultValue = "") String recherche,
                                           @RequestParam(defaultValue = "0") int page,
                                           HttpServletRequest requete, Model modele) {
        Page<IncidentSuivi> incidents = incidentService.rechercherIncidentsActifs(
                null, null, recherche, Pagination.demanderPage(page));
        modele.addAttribute("incidents", incidents.getContent());
        modele.addAttribute("pagination", Pagination.decrire(incidents, requete));
        modele.addAttribute("lignesParStation", stationService.regrouperLignesParStation(
                TableauDeBordControleur.idsStations(incidents.getContent())));
        modele.addAttribute("recherche", recherche);
        return "incidents-en-cours";
    }

    @GetMapping("/passes")
    public String afficherHistorique(@RequestParam(defaultValue = "") String recherche,
                                     @RequestParam(defaultValue = "0") int page,
                                     HttpServletRequest requete, Model modele) {
        Page<Incident> incidents = incidentService.rechercherHistorique(recherche, Pagination.demanderPage(page));
        List<Integer> idsStations = incidents.getContent().stream()
                .map(incident -> incident.getEquipement().getStation().getId())
                .distinct()
                .toList();
        modele.addAttribute("incidents", incidents.getContent());
        modele.addAttribute("evaluations", incidentService.trouverEvaluations(incidents.getContent()));
        modele.addAttribute("pagination", Pagination.decrire(incidents, requete));
        modele.addAttribute("lignesParStation", stationService.regrouperLignesParStation(idsStations));
        modele.addAttribute("recherche", recherche);
        return "incidents-passes";
    }

    @GetMapping("/{idIncident}")
    public String afficherDetail(@PathVariable Integer idIncident, Model modele) {
        Incident incident = incidentService.trouverIncident(idIncident);
        modele.addAttribute("suivi", incidentService.suivreIncident(incident));
        modele.addAttribute("evaluation", incidentService.trouverEvaluation(idIncident).orElse(null));
        return "popups/detail-incident";
    }

    @GetMapping("/nouveau")
    public String afficherDeclaration(Model modele) {
        modele.addAttribute("formulaire", new DeclarationIncidentFormulaire(null, "", ""));
        modele.addAttribute("equipements", equipementService.listerEquipements());
        return "popups/declaration-incident";
    }

    @PostMapping
    public String declarerIncident(@Valid @ModelAttribute("formulaire") DeclarationIncidentFormulaire formulaire,
                                   BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            modele.addAttribute("equipements", equipementService.listerEquipements());
            return "popups/declaration-incident";
        }
        incidentService.declarerIncident(formulaire);
        return REDIRECTION_INCIDENTS;
    }

    @GetMapping("/{idIncident}/modification")
    public String afficherModification(@PathVariable Integer idIncident, Model modele) {
        Incident incident = incidentService.trouverIncident(idIncident);
        modele.addAttribute("idIncident", idIncident);
        modele.addAttribute("formulaire",
                new ModificationIncidentFormulaire(incident.getTitre(), incident.getDescription()));
        return "popups/modification-incident";
    }

    @PutMapping("/{idIncident}")
    public String modifierIncident(@PathVariable Integer idIncident,
                                   @Valid @ModelAttribute("formulaire") ModificationIncidentFormulaire formulaire,
                                   BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            modele.addAttribute("idIncident", idIncident);
            return "popups/modification-incident";
        }
        incidentService.modifierIncident(idIncident, formulaire);
        return REDIRECTION_INCIDENTS;
    }

    @GetMapping("/{idIncident}/affectation")
    public String afficherAffectation(@PathVariable Integer idIncident, Model modele) {
        modele.addAttribute("incident", incidentService.trouverIncident(idIncident));
        modele.addAttribute("prestataires", prestataireService.listerPrestataires());
        return "popups/affectation-incident";
    }

    @PutMapping("/{idIncident}/affectation")
    public String affecterPrestataire(@PathVariable Integer idIncident, @RequestParam Integer idPrestataire) {
        incidentService.affecterPrestataire(idIncident, idPrestataire);
        return REDIRECTION_INCIDENTS;
    }

    @GetMapping("/{idIncident}/resolution")
    public String afficherResolution(@PathVariable Integer idIncident, Model modele) {
        modele.addAttribute("incident", incidentService.trouverIncident(idIncident));
        modele.addAttribute("formulaire", new ResolutionFormulaire(null, ""));
        return "popups/resolution-incident";
    }

    @PutMapping("/{idIncident}/resolution")
    public String resoudreIncident(@PathVariable Integer idIncident,
                                   @Valid @ModelAttribute("formulaire") ResolutionFormulaire formulaire,
                                   BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            modele.addAttribute("incident", incidentService.trouverIncident(idIncident));
            return "popups/resolution-incident";
        }
        incidentService.resoudreIncident(idIncident, formulaire);
        return REDIRECTION_INCIDENTS;
    }

    @GetMapping("/{idIncident}/evaluation")
    public String afficherEvaluation(@PathVariable Integer idIncident, Model modele) {
        modele.addAttribute("incident", incidentService.trouverIncident(idIncident));
        modele.addAttribute("evaluation", incidentService.trouverEvaluation(idIncident).orElse(null));
        return "popups/evaluation-incident";
    }

    @DeleteMapping("/{idIncident}")
    public String supprimerIncident(@PathVariable Integer idIncident) {
        incidentService.supprimerIncident(idIncident);
        return REDIRECTION_INCIDENTS;
    }
}
