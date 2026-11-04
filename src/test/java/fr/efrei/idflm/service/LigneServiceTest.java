package fr.efrei.idflm.service;

import fr.efrei.idflm.exception.LigneIntrouvableException;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.modele.Ligne;
import fr.efrei.idflm.repository.LigneRepository;
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
class LigneServiceTest {

    @Mock
    private LigneRepository ligneRepository;
    @InjectMocks
    private LigneService ligneService;

    @Test
    void supprimerUneLigneQuiDessertDesStationsEstRefuse() {
        Ligne ligneA = new Ligne();
        ligneA.setNom("Ligne A");
        when(ligneRepository.findById(2)).thenReturn(Optional.of(ligneA));
        when(ligneRepository.compterStationsDeLaLigne(2)).thenReturn(5L);

        assertThatThrownBy(() -> ligneService.supprimerLigne(2))
                .isInstanceOf(SuppressionImpossibleException.class);
        verify(ligneRepository, never()).delete(any());
    }

    @Test
    void ligneInexistanteLeveLigneIntrouvable() {
        when(ligneRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ligneService.trouverLigne(99))
                .isInstanceOf(LigneIntrouvableException.class)
                .hasMessage("La ligne 99 n'existe pas");
    }
}
