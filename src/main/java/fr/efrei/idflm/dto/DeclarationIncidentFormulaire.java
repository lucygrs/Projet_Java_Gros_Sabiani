package fr.efrei.idflm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeclarationIncidentFormulaire(
        @NotNull(message = "L'équipement concerné est obligatoire")
        Integer idEquipement,
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
        String titre,
        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description) {
}
