package fr.efrei.idflm.service;

import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.modele.Prestataire;
import fr.efrei.idflm.repository.IncidentRepository;
import fr.efrei.idflm.repository.PrestataireRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestataireServiceTest {

    @Mock
    private PrestataireRepository prestataireRepository;
    @Mock
    private IncidentRepository incidentRepository;
    @InjectMocks
    private PrestataireService prestataireService;

    @Test
    void supprimerUnPrestataireAffecteADesIncidentsEstRefuse() {
        Prestataire prestataire = new Prestataire();
        prestataire.setNom("Escalator & Co");
        when(prestataireRepository.findById(1)).thenReturn(Optional.of(prestataire));
        when(incidentRepository.existsByPrestataireId(1)).thenReturn(true);

        assertThatThrownBy(() -> prestataireService.supprimerPrestataire(1))
                .isInstanceOf(SuppressionImpossibleException.class);
        verify(prestataireRepository, never()).delete(any());
    }
}
