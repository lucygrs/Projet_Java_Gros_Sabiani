package fr.efrei.idflm.dto;

import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.modele.NiveauAttention;

import java.time.Duration;

public record IncidentSuivi(Incident incident, NiveauAttention niveauAttention, Duration dureeOuverture) {
}
