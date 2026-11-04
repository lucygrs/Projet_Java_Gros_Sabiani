package fr.efrei.idflm.controleur;

import fr.efrei.idflm.dto.IncidentSuivi;
import fr.efrei.idflm.modele.StatutIncident;
import fr.efrei.idflm.service.IncidentService;
import fr.efrei.idflm.service.StationService;
import fr.efrei.idflm.service.TableauDeBordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TableauDeBordControleur {

    private final TableauDeBordService tableauDeBordService;
    private final IncidentService incidentService;
    private final StationService stationService;

    public TableauDeBordControleur(TableauDeBordService tableauDeBordService, IncidentService incidentService,
                                   StationService stationService) {
        this.tableauDeBordService = tableauDeBordService;
        this.incidentService = incidentService;
        this.stationService = stationService;
    }

    @GetMapping("/tableau-de-bord")
    public String afficherTableauDeBord(@RequestParam(required = false) Integer station,
                                        @RequestParam(required = false) StatutIncident statut,
                                        @RequestParam(defaultValue = "") String recherche,
                                        @RequestParam(defaultValue = "0") int page,
                                        HttpServletRequest requete, Model modele) {
        Page<IncidentSuivi> incidents = incidentService.rechercherIncidentsActifs(
                station, statut, recherche, Pagination.demanderPage(page));
        modele.addAttribute("indicateurs", tableauDeBordService.calculerIndicateurs());
        modele.addAttribute("incidents", incidents.getContent());
        modele.addAttribute("pagination", Pagination.decrire(incidents, requete));
        modele.addAttribute("lignesParStation", stationService.regrouperLignesParStation(
                idsStations(incidents.getContent())));
        modele.addAttribute("stations", stationService.listerStations());
        modele.addAttribute("stationChoisie", station);
        modele.addAttribute("statutChoisi", statut);
        modele.addAttribute("recherche", recherche);
        return "tableau-de-bord";
    }

    static List<Integer> idsStations(List<IncidentSuivi> incidents) {
        return incidents.stream()
                .map(suivi -> suivi.incident().getEquipement().getStation().getId())
                .distinct()
                .toList();
    }
}
