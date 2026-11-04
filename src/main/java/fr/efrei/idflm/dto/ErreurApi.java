package fr.efrei.idflm.dto;

import java.util.List;

public record ErreurApi(int statut, String message, List<String> details) {
}
