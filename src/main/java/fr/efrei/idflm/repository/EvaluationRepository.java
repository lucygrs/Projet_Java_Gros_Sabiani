package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {

    Optional<Evaluation> findByIncidentId(Integer idIncident);

    boolean existsByIncidentId(Integer idIncident);

    List<Evaluation> findByIncidentIdIn(List<Integer> idsIncidents);
}
