package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.StationFormulaire;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.modele.Ligne;
import fr.efrei.idflm.modele.LigneStation;
import fr.efrei.idflm.modele.Station;
import fr.efrei.idflm.repository.EquipementRepository;
import fr.efrei.idflm.repository.LigneStationRepository;
import fr.efrei.idflm.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    private static final Integer ID_LIGNE = 2;
    private static final Integer ID_STATION = 5;

    @Mock
    private StationRepository stationRepository;
    @Mock
    private LigneStationRepository ligneStationRepository;
    @Mock
    private EquipementRepository equipementRepository;
    @Mock
    private LigneService ligneService;
    @InjectMocks
    private StationService stationService;

    @Test
    void stationDejaExistanteEstReutiliseeQuandOnLAjouteAUneLigne() {
        Station chatelet = station("Châtelet");
        when(ligneService.trouverLigne(ID_LIGNE)).thenReturn(new Ligne());
        when(stationRepository.findByNomIgnoreCase("châtelet")).thenReturn(Optional.of(chatelet));
        when(ligneStationRepository.findByLigneIdAndStationId(ID_LIGNE, ID_STATION)).thenReturn(Optional.empty());

        Station ajoutee = stationService.ajouterStationALaLigne(ID_LIGNE, new StationFormulaire("châtelet"));

        assertThat(ajoutee).isSameAs(chatelet);
        verify(stationRepository, never()).save(any());
        verify(ligneStationRepository).save(any(LigneStation.class));
    }

    @Test
    void retirerLaDerniereLigneDUneStationEquipeeEstRefuse() {
        when(stationRepository.findById(ID_STATION)).thenReturn(Optional.of(station("Boissy-Saint-Léger")));
        when(ligneStationRepository.existsByStationId(ID_STATION)).thenReturn(false);
        when(equipementRepository.existsByStationId(ID_STATION)).thenReturn(true);

        assertThatThrownBy(() -> stationService.retirerStationDeLaLigne(ID_LIGNE, ID_STATION))
                .isInstanceOf(SuppressionImpossibleException.class)
                .hasMessageContaining("a encore des équipements");
        verify(stationRepository, never()).delete(any());
    }

    private Station station(String nom) {
        Station station = new Station();
        station.setId(ID_STATION);
        station.setNom(nom);
        return station;
    }
}
