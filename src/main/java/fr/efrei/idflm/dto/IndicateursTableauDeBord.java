package fr.efrei.idflm.dto;

public record IndicateursTableauDeBord(
        long signalementsEnCours,
        long equipements,
        long lignes,
        long agentsDeSignalement,
        long responsablesExploitation) {
}
