package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Ligne;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LigneRepository extends JpaRepository<Ligne, Integer> {

    Page<Ligne> findByNomContainingIgnoreCase(String nom, Pageable pagination);

    @Query(value = "SELECT COUNT(*) FROM ligne_station WHERE id_ligne = :idLigne", nativeQuery = true)
    long compterStationsDeLaLigne(Integer idLigne);
}
