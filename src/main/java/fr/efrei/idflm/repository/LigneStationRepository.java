package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.LigneStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LigneStationRepository extends JpaRepository<LigneStation, Integer> {

    List<LigneStation> findByStationIdIn(List<Integer> idsStations);

    Optional<LigneStation> findByLigneIdAndStationId(Integer idLigne, Integer idStation);

    boolean existsByStationId(Integer idStation);
}
