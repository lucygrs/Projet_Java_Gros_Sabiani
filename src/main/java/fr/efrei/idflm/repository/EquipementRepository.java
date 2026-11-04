package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Equipement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EquipementRepository extends JpaRepository<Equipement, Integer> {

    boolean existsByReference(String reference);

    boolean existsByReferenceAndIdNot(String reference, Integer id);

    List<Equipement> findByStationIdOrderByNomAsc(Integer idStation);

    @Query("""
            SELECT e FROM Equipement e
            WHERE LOWER(e.nom) LIKE LOWER(CONCAT('%', :recherche, '%'))
            OR LOWER(e.reference) LIKE LOWER(CONCAT('%', :recherche, '%'))
            OR LOWER(e.station.nom) LIKE LOWER(CONCAT('%', :recherche, '%'))
            ORDER BY e.nom
            """)
    Page<Equipement> rechercherEquipements(String recherche, Pageable pagination);
}
