package fr.efrei.idflm.modele;

import fr.efrei.idflm.exception.ResolutionImpossibleException;
import fr.efrei.idflm.exception.TransitionInterditeException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncidentTest {

    private static final LocalDateTime OUVERTURE = LocalDateTime.of(2026, 11, 1, 8, 0);

    @Test
    void incidentDeclareDemarreNonAffecteEtSansPrestataire() {
        Incident incident = declarerIncident();

        assertThat(incident.getStatut()).isEqualTo(StatutIncident.NON_AFFECTE);
        assertThat(incident.aUnPrestataire()).isFalse();
        assertThat(incident.getDateOuverture()).isEqualTo(OUVERTURE);
    }

    @Test
    void incidentAffecteEstReaffectableTantQuIlNEstPasResolu() {
        Incident incident = declarerIncident();
        Prestataire premier = new Prestataire();
        Prestataire second = new Prestataire();

        incident.affecter(premier);
        incident.affecter(second);

        assertThat(incident.getStatut()).isEqualTo(StatutIncident.AFFECTE);
        assertThat(incident.getPrestataire()).isSameAs(second);
    }

    @Test
    void incidentResoluConserveSaDateDeResolution() {
        Incident incident = declarerIncidentResolu();

        assertThat(incident.getStatut()).isEqualTo(StatutIncident.RESOLU);
        assertThat(incident.getDateResolution()).isEqualTo(OUVERTURE.plusHours(5));
    }

    @Test
    void incidentResoluNePeutPlusEtreAffecte() {
        Incident incident = declarerIncidentResolu();

        assertThatThrownBy(() -> incident.affecter(new Prestataire()))
                .isInstanceOf(TransitionInterditeException.class)
                .hasMessageContaining("ne peut plus être affecté");
    }

    @Test
    void incidentResoluNePeutPlusEtreModifie() {
        Incident incident = declarerIncidentResolu();

        assertThatThrownBy(() -> incident.modifierInformations("Nouveau titre", null))
                .isInstanceOf(TransitionInterditeException.class);
    }

    @Test
    void incidentResoluNePeutPasEtreResoluUneSecondeFois() {
        Incident incident = declarerIncidentResolu();

        assertThatThrownBy(() -> incident.resoudre(OUVERTURE.plusHours(6)))
                .isInstanceOf(TransitionInterditeException.class);
    }

    @Test
    void incidentSansPrestataireNePeutPasEtreResolu() {
        Incident incident = declarerIncident();

        assertThatThrownBy(() -> incident.resoudre(OUVERTURE.plusHours(1)))
                .isInstanceOf(ResolutionImpossibleException.class)
                .hasMessageContaining("aucun prestataire");
    }

    @Test
    void resolutionAnterieureALOuvertureEstRefusee() {
        Incident incident = declarerIncident();
        incident.affecter(new Prestataire());

        assertThatThrownBy(() -> incident.resoudre(OUVERTURE.minusMinutes(1)))
                .isInstanceOf(ResolutionImpossibleException.class);
    }

    @Test
    void dureeOuvertureDUnIncidentActifEstCalculeeAvecLHorloge() {
        ZoneId fuseau = ZoneId.of("Europe/Paris");
        Clock horloge = Clock.fixed(OUVERTURE.plusHours(30).atZone(fuseau).toInstant(), fuseau);

        assertThat(declarerIncident().dureeOuverture(horloge)).isEqualTo(Duration.ofHours(30));
    }

    private Incident declarerIncident() {
        return Incident.declarer(new Equipement(), "Escalator cassé", "Marche arrachée", OUVERTURE);
    }

    private Incident declarerIncidentResolu() {
        Incident incident = declarerIncident();
        incident.affecter(new Prestataire());
        incident.resoudre(OUVERTURE.plusHours(5));
        return incident;
    }
}
