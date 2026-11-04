package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Equipement;
import fr.efrei.idflm.modele.EtatEquipement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EquipementRepositoryTest {

    private static final String REFERENCE_EXISTANTE = "89541";

    @Autowired
    private EquipementRepository equipementRepository;

    @Autowired
    private StationRepository stationRepository;

    @Test
    void referenceDejaUtiliseeEstDetectee() {
        assertThat(equipementRepository.existsByReference(REFERENCE_EXISTANTE)).isTrue();
        assertThat(equipementRepository.existsByReference("00000")).isFalse();
    }

    @Test
    void enregistrerUneReferenceEnDoubleEstRefuseParLaBase() {
        Equipement doublon = creerAscenseur(REFERENCE_EXISTANTE);

        assertThatThrownBy(() -> equipementRepository.saveAndFlush(doublon))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void nouvelEquipementObtientUnIdentifiantLibre() {
        Equipement equipement = creerAscenseur("99999");

        assertThat(equipementRepository.saveAndFlush(equipement).getId()).isNotNull();
    }

    private Equipement creerAscenseur(String reference) {
        Equipement equipement = new Equipement();
        equipement.setReference(reference);
        equipement.setNom("Ascenseur de test");
        equipement.setType("Ascenseur");
        equipement.setEtat(EtatEquipement.EN_SERVICE);
        equipement.setStation(stationRepository.findAll().getFirst());
        return equipement;
    }
}
