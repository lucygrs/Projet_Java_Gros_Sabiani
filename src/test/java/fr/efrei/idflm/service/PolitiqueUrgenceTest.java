package fr.efrei.idflm.service;

import fr.efrei.idflm.modele.Equipement;
import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.modele.NiveauAttention;
import fr.efrei.idflm.modele.Prestataire;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class PolitiqueUrgenceTest {

    private static final ZoneId FUSEAU = ZoneId.of("Europe/Paris");
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 4, 12, 0);

    private final PolitiqueUrgence politiqueUrgence =
            new PolitiqueUrgence(Clock.fixed(MAINTENANT.atZone(FUSEAU).toInstant(), FUSEAU));

    @Test
    void incidentNonAffecteOuvertDepuis47HeuresEstVert() {
        assertThat(politiqueUrgence.evaluer(incidentOuvertDepuis(Duration.ofHours(47))))
                .isEqualTo(NiveauAttention.VERT);
    }

    @Test
    void incidentNonAffecteOuvertDepuis48HeuresPileEstOrange() {
        assertThat(politiqueUrgence.evaluer(incidentOuvertDepuis(Duration.ofHours(48))))
                .isEqualTo(NiveauAttention.ORANGE);
    }

    @Test
    void incidentNonAffecteOuvertDepuis71Heures59EstEncoreOrange() {
        assertThat(politiqueUrgence.evaluer(incidentOuvertDepuis(Duration.ofHours(71).plusMinutes(59))))
                .isEqualTo(NiveauAttention.ORANGE);
    }

    @Test
    void incidentNonAffecteOuvertDepuis72HeuresEstRouge() {
        assertThat(politiqueUrgence.evaluer(incidentOuvertDepuis(Duration.ofHours(72))))
                .isEqualTo(NiveauAttention.ROUGE);
    }

    @Test
    void incidentAffecteEstPrisEnChargeQuelleQueSoitSaDuree() {
        Incident incident = incidentOuvertDepuis(Duration.ofHours(100));
        incident.affecter(new Prestataire());

        assertThat(politiqueUrgence.evaluer(incident)).isEqualTo(NiveauAttention.PRIS_EN_CHARGE);
    }

    private Incident incidentOuvertDepuis(Duration duree) {
        return Incident.declarer(new Equipement(), "Panne", null, MAINTENANT.minus(duree));
    }
}
