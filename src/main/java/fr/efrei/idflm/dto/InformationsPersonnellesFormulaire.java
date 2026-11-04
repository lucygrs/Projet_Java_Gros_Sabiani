package fr.efrei.idflm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InformationsPersonnellesFormulaire(
        @NotBlank(message = "L'identifiant est obligatoire")
        @Size(max = 100, message = "L'identifiant ne doit pas dépasser 100 caractères")
        String identifiant,
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,
        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom) {
}
