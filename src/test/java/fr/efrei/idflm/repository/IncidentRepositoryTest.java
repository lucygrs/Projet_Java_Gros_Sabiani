package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.modele.StatutIncident;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IncidentRepositoryTest {

    private static final Pageable PREMIERE_PAGE = PageRequest.of(0, 50);
    private static final String SANS_RECHERCHE = "";

    @Autowired
    private IncidentRepository incidentRepository;

    @Test
    void historiqueDesIncidentsResolusEstTrieDuPlusRecentAuPlusAncien() {
        List<Incident> historique = incidentRepository
                .rechercherIncidentsResolus(SANS_RECHERCHE, PREMIERE_PAGE).getContent();

        assertThat(historique).isNotEmpty()
                .allMatch(incident -> incident.getStatut() == StatutIncident.RESOLU)
                .extracting(Incident::getDateResolution)
                .isSortedAccordingTo((premiere, seconde) -> seconde.compareTo(premiere));
    }

    @Test
    void incidentsActifsNeContiennentAucunIncidentResolu() {
        List<Incident> actifs = incidentRepository
                .rechercherIncidentsActifs(null, null, SANS_RECHERCHE, PREMIERE_PAGE).getContent();

        assertThat(actifs).hasSize((int) incidentRepository.countByStatutNot(StatutIncident.RESOLU))
                .noneMatch(incident -> incident.getStatut() == StatutIncident.RESOLU);
    }

    @Test
    void incidentsActifsSontFiltresParStationEtParStatut() {
        Integer idChatelet = 2;

        List<Incident> actifsAChatelet = incidentRepository
                .rechercherIncidentsActifs(idChatelet, StatutIncident.NON_AFFECTE, SANS_RECHERCHE, PREMIERE_PAGE)
                .getContent();

        assertThat(actifsAChatelet).isNotEmpty()
                .allMatch(incident -> incident.getEquipement().getStation().getId().equals(idChatelet))
                .allMatch(incident -> incident.getStatut() == StatutIncident.NON_AFFECTE);
    }

    @Test
    void incidentsDUnEquipementSontTriesParDateOuverture() {
        Integer idEscalierAvecDeuxIncidents = 2;

        List<Incident> incidents = incidentRepository
                .findByEquipementIdOrderByDateOuvertureDesc(idEscalierAvecDeuxIncidents);

        assertThat(incidents).hasSize(2)
                .extracting(Incident::getDateOuverture)
                .isSortedAccordingTo((premiere, seconde) -> seconde.compareTo(premiere));
    }
}
