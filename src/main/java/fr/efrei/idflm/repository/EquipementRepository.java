package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Equipement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EquipementRepository extends JpaRepository<Equipement, Integer> {

    boolean existsByReference(String reference);

    boolean existsByReferenceAndIdNot(String reference, Integer id);

    boolean existsByStationId(Integer idStation);

    @Query("""
            SELECT e FROM Equipement e
            WHERE (:idStation IS NULL OR e.station.id = :idStation)
            AND (LOWER(e.nom) LIKE LOWER(CONCAT('%', :recherche, '%'))
                OR LOWER(e.reference) LIKE LOWER(CONCAT('%', :recherche, '%'))
                OR LOWER(e.station.nom) LIKE LOWER(CONCAT('%', :recherche, '%')))
            ORDER BY e.nom
            """)
    Page<Equipement> rechercherEquipements(Integer idStation, String recherche, Pageable pagination);
}
