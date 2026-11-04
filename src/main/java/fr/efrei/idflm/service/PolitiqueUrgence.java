package fr.efrei.idflm.service;

import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.modele.NiveauAttention;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

@Component
public class PolitiqueUrgence {

    private static final Duration SEUIL_ORANGE = Duration.ofHours(48);
    private static final Duration SEUIL_ROUGE = Duration.ofHours(72);

    private final Clock horloge;

    public PolitiqueUrgence(Clock horloge) {
        this.horloge = horloge;
    }

    public NiveauAttention evaluer(Incident incident) {
        if (incident.aUnPrestataire()) {
            return NiveauAttention.PRIS_EN_CHARGE;
        }
        Duration dureeOuverture = incident.dureeOuverture(horloge);
        if (dureeOuverture.compareTo(SEUIL_ROUGE) >= 0) {
            return NiveauAttention.ROUGE;
        }
        if (dureeOuverture.compareTo(SEUIL_ORANGE) >= 0) {
            return NiveauAttention.ORANGE;
        }
        return NiveauAttention.VERT;
    }
}
