package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Evaluation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EvaluationRepositoryTest {

    private static final Integer ID_INCIDENT_DEJA_EVALUE = 9;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Test
    void secondeEvaluationDuMemeIncidentEstRefuseeParLaBase() {
        Evaluation seconde = new Evaluation(
                incidentRepository.findById(ID_INCIDENT_DEJA_EVALUE).orElseThrow(), 5, null, LocalDateTime.now());

        assertThatThrownBy(() -> evaluationRepository.saveAndFlush(seconde))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
