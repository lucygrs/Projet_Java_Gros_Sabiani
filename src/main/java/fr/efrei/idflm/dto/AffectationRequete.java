package fr.efrei.idflm.dto;

import jakarta.validation.constraints.NotNull;

public record AffectationRequete(
        @NotNull(message = "Le prestataire est obligatoire")
        Integer idPrestataire) {
}
