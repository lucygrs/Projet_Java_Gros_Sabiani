package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.EquipementFormulaire;
import fr.efrei.idflm.exception.ReferenceDejaUtiliseeException;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.modele.Equipement;
import fr.efrei.idflm.modele.EtatEquipement;
import fr.efrei.idflm.repository.EquipementRepository;
import fr.efrei.idflm.repository.IncidentRepository;
import fr.efrei.idflm.repository.StationRepository;
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
class EquipementServiceTest {

    @Mock
    private EquipementRepository equipementRepository;
    @Mock
    private StationRepository stationRepository;
    @Mock
    private IncidentRepository incidentRepository;
    @InjectMocks
    private EquipementService equipementService;

    @Test
    void creerUnEquipementAvecUneReferenceDejaUtiliseeEstRefuse() {
        when(equipementRepository.existsByReference("89541")).thenReturn(true);
        EquipementFormulaire formulaire = new EquipementFormulaire(
                "89541", "Portique", null, "Portique", 1, EtatEquipement.EN_SERVICE);

        assertThatThrownBy(() -> equipementService.creerEquipement(formulaire))
                .isInstanceOf(ReferenceDejaUtiliseeException.class)
                .hasMessageContaining("89541");
        verify(equipementRepository, never()).save(any());
    }

    @Test
    void supprimerUnEquipementQuiADesIncidentsEstRefuse() {
        Equipement equipement = new Equipement();
        equipement.setNom("Ascenseur quai A");
        when(equipementRepository.findById(3)).thenReturn(Optional.of(equipement));
        when(incidentRepository.existsByEquipementId(3)).thenReturn(true);

        assertThatThrownBy(() -> equipementService.supprimerEquipement(3))
                .isInstanceOf(SuppressionImpossibleException.class);
        verify(equipementRepository, never()).delete(any());
    }
}
