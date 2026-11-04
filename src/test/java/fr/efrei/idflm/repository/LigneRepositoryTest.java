package fr.efrei.idflm.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LigneRepositoryTest {

    @Autowired
    private LigneRepository ligneRepository;

    @Test
    void nombreDeStationsEstCompteParLigne() {
        Integer idLigne1 = 1;
        Integer idLigneA = 2;

        assertThat(ligneRepository.compterStationsDeLaLigne(idLigne1)).isEqualTo(4);
        assertThat(ligneRepository.compterStationsDeLaLigne(idLigneA)).isEqualTo(5);
    }
}
