package fr.efrei.idflm.api;

import fr.efrei.idflm.dto.AffectationRequete;
import fr.efrei.idflm.dto.DeclarationIncidentFormulaire;
import fr.efrei.idflm.dto.ErreurApi;
import fr.efrei.idflm.dto.IncidentReponse;
import fr.efrei.idflm.dto.ResolutionFormulaire;
import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@Tag(name = "Incidents", description = "Consultation des incidents et modification de leur état (F12)")
public class IncidentApiControleur {

    private final IncidentService incidentService;

    public IncidentApiControleur(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    @Operation(summary = "Lister les incidents actifs, du plus ancien au plus récent")
    public List<IncidentReponse> listerIncidentsActifs() {
        return incidentService.listerSignalementsEnCours().stream()
                .map(IncidentReponse::depuis)
                .toList();
    }

    @GetMapping("/resolus")
    @Operation(summary = "Lister l'historique des incidents résolus, du plus récent au plus ancien (responsable)")
    public List<IncidentReponse> listerIncidentsResolus() {
        return incidentService.listerIncidentsResolus().stream()
                .map(IncidentReponse::depuis)
                .toList();
    }

    @GetMapping("/{idIncident}")
    @Operation(summary = "Consulter un incident")
    @ApiResponse(responseCode = "404", description = "Incident inexistant",
            content = @Content(schema = @Schema(implementation = ErreurApi.class)))
    public IncidentReponse consulterIncident(@PathVariable Integer idIncident) {
        return decrire(incidentService.trouverIncident(idIncident));
    }

    // ResponseEntity sert uniquement ici, pour renvoyer 201 et l'adresse du nouvel incident
    @PostMapping
    @Operation(summary = "Déclarer un incident sur un équipement")
    @ApiResponse(responseCode = "201", description = "Incident déclaré")
    public ResponseEntity<IncidentReponse> declarerIncident(
            @Valid @RequestBody DeclarationIncidentFormulaire formulaire) {
        Incident incident = incidentService.declarerIncident(formulaire);
        URI adresse = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{idIncident}")
                .buildAndExpand(incident.getId())
                .toUri();
        return ResponseEntity.created(adresse).body(decrire(incident));
    }

    @PutMapping("/{idIncident}/prestataire")
    @Operation(summary = "Affecter ou réaffecter un prestataire (responsable)")
    @ApiResponse(responseCode = "409", description = "Incident déjà résolu",
            content = @Content(schema = @Schema(implementation = ErreurApi.class)))
    public IncidentReponse affecterPrestataire(@PathVariable Integer idIncident,
                                               @Valid @RequestBody AffectationRequete requete) {
        return decrire(incidentService.affecterPrestataire(idIncident, requete.idPrestataire()));
    }

    @PutMapping("/{idIncident}/resolution")
    @Operation(summary = "Résoudre un incident et évaluer le prestataire")
    @ApiResponse(responseCode = "409", description = "Incident déjà résolu ou sans prestataire",
            content = @Content(schema = @Schema(implementation = ErreurApi.class)))
    public IncidentReponse resoudreIncident(@PathVariable Integer idIncident,
                                            @Valid @RequestBody ResolutionFormulaire formulaire) {
        return decrire(incidentService.resoudreIncident(idIncident, formulaire));
    }

    private IncidentReponse decrire(Incident incident) {
        return IncidentReponse.depuis(incidentService.suivreIncident(incident));
    }
}
