package fr.efrei.idflm.dto;

import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.modele.NiveauAttention;
import fr.efrei.idflm.modele.StatutIncident;

import java.time.LocalDateTime;

public record IncidentReponse(
        Integer id,
        String titre,
        String description,
        StatutIncident statut,
        NiveauAttention niveauAttention,
        long dureeOuvertureEnHeures,
        LocalDateTime dateOuverture,
        LocalDateTime dateResolution,
        String referenceEquipement,
        String nomEquipement,
        String station,
        String prestataire) {

    // Le niveau d'attention ne concerne que les incidents actifs du tableau de bord
    public static IncidentReponse depuis(IncidentSuivi suivi) {
        Incident incident = suivi.incident();
        return new IncidentReponse(
                incident.getId(),
                incident.getTitre(),
                incident.getDescription(),
                incident.getStatut(),
                incident.estResolu() ? null : suivi.niveauAttention(),
                suivi.dureeOuverture().toHours(),
                incident.getDateOuverture(),
                incident.getDateResolution(),
                incident.getEquipement().getReference(),
                incident.getEquipement().getNom(),
                incident.getEquipement().getStation().getNom(),
                incident.aUnPrestataire() ? incident.getPrestataire().getNom() : null);
    }
}
