package fr.efrei.idflm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PrestataireFormulaire(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,
        @NotBlank(message = "Le contact est obligatoire")
        @Size(max = 100, message = "Le contact ne doit pas dépasser 100 caractères")
        String contact) {
}
