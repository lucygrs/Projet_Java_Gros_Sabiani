package fr.efrei.idflm.controleur;

import fr.efrei.idflm.dto.EquipementFormulaire;
import fr.efrei.idflm.modele.Equipement;
import fr.efrei.idflm.modele.EtatEquipement;
import fr.efrei.idflm.service.EquipementService;
import fr.efrei.idflm.service.IncidentService;
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
@RequestMapping("/equipements")
public class EquipementControleur {

    private static final String REDIRECTION_EQUIPEMENTS = "redirect:/equipements";
    private static final String POPUP_FORMULAIRE = "popups/formulaire-equipement";

    private final EquipementService equipementService;
    private final StationService stationService;
    private final IncidentService incidentService;

    public EquipementControleur(EquipementService equipementService, StationService stationService,
                                IncidentService incidentService) {
        this.equipementService = equipementService;
        this.stationService = stationService;
        this.incidentService = incidentService;
    }

    @GetMapping
    public String afficherEquipements(@RequestParam(required = false) Integer station,
                                      @RequestParam(defaultValue = "") String recherche,
                                      @RequestParam(defaultValue = "0") int page,
                                      HttpServletRequest requete, Model modele) {
        Page<Equipement> equipements = equipementService.rechercherEquipements(
                station, recherche, Pagination.demanderPage(page));
        List<Integer> idsStations = equipements.getContent().stream()
                .map(equipement -> equipement.getStation().getId())
                .distinct()
                .toList();
        modele.addAttribute("equipements", equipements.getContent());
        modele.addAttribute("pagination", Pagination.decrire(equipements, requete));
        modele.addAttribute("lignesParStation", stationService.regrouperLignesParStation(idsStations));
        modele.addAttribute("stationFiltree", station == null ? null : stationService.trouverStation(station));
        modele.addAttribute("recherche", recherche);
        return "equipements";
    }

    @GetMapping("/{idEquipement}/fiche")
    public String afficherFiche(@PathVariable Integer idEquipement, Model modele) {
        modele.addAttribute("equipement", equipementService.trouverEquipement(idEquipement));
        modele.addAttribute("incidents", incidentService.listerIncidentsDeLEquipement(idEquipement));
        return "popups/fiche-equipement";
    }

    @GetMapping("/nouveau")
    public String afficherCreation(Model modele) {
        modele.addAttribute("formulaire", new EquipementFormulaire("", "", "", "", null, EtatEquipement.EN_SERVICE));
        return preparerFormulaire(null, modele);
    }

    @PostMapping
    public String creerEquipement(@Valid @ModelAttribute("formulaire") EquipementFormulaire formulaire,
                                  BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            return preparerFormulaire(null, modele);
        }
        equipementService.creerEquipement(formulaire);
        return REDIRECTION_EQUIPEMENTS;
    }

    @GetMapping("/{idEquipement}/modification")
    public String afficherModification(@PathVariable Integer idEquipement, Model modele) {
        Equipement equipement = equipementService.trouverEquipement(idEquipement);
        modele.addAttribute("formulaire", new EquipementFormulaire(equipement.getReference(), equipement.getNom(),
                equipement.getDescription(), equipement.getType(), equipement.getStation().getId(),
                equipement.getEtat()));
        return preparerFormulaire(idEquipement, modele);
    }

    @PutMapping("/{idEquipement}")
    public String modifierEquipement(@PathVariable Integer idEquipement,
                                     @Valid @ModelAttribute("formulaire") EquipementFormulaire formulaire,
                                     BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            return preparerFormulaire(idEquipement, modele);
        }
        equipementService.modifierEquipement(idEquipement, formulaire);
        return REDIRECTION_EQUIPEMENTS;
    }

    @DeleteMapping("/{idEquipement}")
    public String supprimerEquipement(@PathVariable Integer idEquipement) {
        equipementService.supprimerEquipement(idEquipement);
        return REDIRECTION_EQUIPEMENTS;
    }

    private String preparerFormulaire(Integer idEquipement, Model modele) {
        modele.addAttribute("idEquipement", idEquipement);
        modele.addAttribute("stations", stationService.listerStations());
        modele.addAttribute("etats", EtatEquipement.values());
        return POPUP_FORMULAIRE;
    }
}
