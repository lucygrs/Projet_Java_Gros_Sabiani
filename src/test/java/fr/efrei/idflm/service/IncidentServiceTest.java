package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.DeclarationIncidentFormulaire;
import fr.efrei.idflm.dto.ResolutionFormulaire;
import fr.efrei.idflm.exception.IncidentIntrouvableException;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.exception.TransitionInterditeException;
import fr.efrei.idflm.modele.Equipement;
import fr.efrei.idflm.modele.EtatEquipement;
import fr.efrei.idflm.modele.Evaluation;
import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.modele.Prestataire;
import fr.efrei.idflm.modele.StatutIncident;
import fr.efrei.idflm.repository.EquipementRepository;
import fr.efrei.idflm.repository.EvaluationRepository;
import fr.efrei.idflm.repository.IncidentRepository;
import fr.efrei.idflm.repository.PrestataireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    private static final ZoneId FUSEAU = ZoneId.of("Europe/Paris");
    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 4, 12, 0);
    private static final Integer ID_INCIDENT = 7;
    private static final Integer ID_EQUIPEMENT = 3;

    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private EquipementRepository equipementRepository;
    @Mock
    private PrestataireRepository prestataireRepository;
    @Mock
    private EvaluationRepository evaluationRepository;

    private IncidentService incidentService;
    private Equipement equipement;

    @BeforeEach
    void preparer() {
        Clock horlogeFigee = Clock.fixed(MAINTENANT.atZone(FUSEAU).toInstant(), FUSEAU);
        incidentService = new IncidentService(incidentRepository, equipementRepository, prestataireRepository,
                evaluationRepository, new PolitiqueUrgence(horlogeFigee), horlogeFigee);
        equipement = new Equipement();
        equipement.setId(ID_EQUIPEMENT);
        equipement.setEtat(EtatEquipement.EN_SERVICE);
    }

    @Test
    void declarerIncidentDateLOuvertureAvecLHorlogeEtMetLEquipementEnPanne() {
        when(equipementRepository.findById(ID_EQUIPEMENT)).thenReturn(Optional.of(equipement));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(appel -> appel.getArgument(0));

        Incident incident = incidentService.declarerIncident(
                new DeclarationIncidentFormulaire(ID_EQUIPEMENT, "Escalator cassé", null));

        assertThat(incident.getDateOuverture()).isEqualTo(MAINTENANT);
        assertThat(incident.getStatut()).isEqualTo(StatutIncident.NON_AFFECTE);
        assertThat(equipement.getEtat()).isEqualTo(EtatEquipement.EN_PANNE);
    }

    @Test
    void resoudreIncidentEnregistreLEvaluationEtRemetLEquipementEnService() {
        Incident incident = incidentAffecteOuvertLaVeille();
        when(incidentRepository.findById(ID_INCIDENT)).thenReturn(Optional.of(incident));

        incidentService.resoudreIncident(ID_INCIDENT, new ResolutionFormulaire(4, "Rapide"));

        ArgumentCaptor<Evaluation> evaluation = ArgumentCaptor.forClass(Evaluation.class);
        verify(evaluationRepository).save(evaluation.capture());
        assertThat(evaluation.getValue().getNote()).isEqualTo(4);
        assertThat(incident.getDateResolution()).isEqualTo(MAINTENANT);
        assertThat(equipement.getEtat()).isEqualTo(EtatEquipement.EN_SERVICE);
    }

    @Test
    void resoudreIncidentLaisseLEquipementEnPanneSiUnAutreIncidentEstActif() {
        Incident incident = incidentAffecteOuvertLaVeille();
        when(incidentRepository.findById(ID_INCIDENT)).thenReturn(Optional.of(incident));
        when(incidentRepository.existsByEquipementIdAndStatutNotAndIdNot(ID_EQUIPEMENT, StatutIncident.RESOLU, null))
                .thenReturn(true);

        incidentService.resoudreIncident(ID_INCIDENT, new ResolutionFormulaire(3, null));

        assertThat(equipement.getEtat()).isEqualTo(EtatEquipement.EN_PANNE);
    }

    @Test
    void incidentInexistantLeveIncidentIntrouvable() {
        when(incidentRepository.findById(ID_INCIDENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.trouverIncident(ID_INCIDENT))
                .isInstanceOf(IncidentIntrouvableException.class)
                .hasMessage("L'incident 7 n'existe pas");
    }

    @Test
    void affecterUnIncidentResoluLeveTransitionInterdite() {
        Incident incident = incidentAffecteOuvertLaVeille();
        incident.resoudre(MAINTENANT);
        when(incidentRepository.findById(ID_INCIDENT)).thenReturn(Optional.of(incident));
        when(prestataireRepository.findById(1)).thenReturn(Optional.of(new Prestataire()));

        assertThatThrownBy(() -> incidentService.affecterPrestataire(ID_INCIDENT, 1))
                .isInstanceOf(TransitionInterditeException.class);
    }

    @Test
    void supprimerUnIncidentResoluEstRefuse() {
        Incident incident = incidentAffecteOuvertLaVeille();
        incident.resoudre(MAINTENANT);
        when(incidentRepository.findById(ID_INCIDENT)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> incidentService.supprimerIncident(ID_INCIDENT))
                .isInstanceOf(SuppressionImpossibleException.class);
        verify(incidentRepository, never()).delete(any());
    }

    private Incident incidentAffecteOuvertLaVeille() {
        equipement.signalerPanne();
        Incident incident = Incident.declarer(equipement, "Escalator cassé", null, MAINTENANT.minusDays(1));
        incident.affecter(new Prestataire());
        return incident;
    }
}
