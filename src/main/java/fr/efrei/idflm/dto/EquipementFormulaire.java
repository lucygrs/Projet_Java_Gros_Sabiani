package fr.efrei.idflm.dto;

import fr.efrei.idflm.modele.EtatEquipement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipementFormulaire(
        @NotBlank(message = "La référence est obligatoire")
        @Size(max = 50, message = "La référence ne doit pas dépasser 50 caractères")
        String reference,
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,
        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,
        @NotBlank(message = "Le type est obligatoire")
        @Size(max = 100, message = "Le type ne doit pas dépasser 100 caractères")
        String type,
        @NotNull(message = "La station est obligatoire")
        Integer idStation,
        @NotNull(message = "L'état est obligatoire")
        EtatEquipement etat) {
}
