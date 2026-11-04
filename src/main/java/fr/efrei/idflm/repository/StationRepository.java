package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Station;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, Integer> {

    List<Station> findAllByOrderByNomAsc();

    @Query("""
            SELECT ls.station FROM LigneStation ls
            WHERE ls.ligne.id = :idLigne
            AND LOWER(ls.station.nom) LIKE LOWER(CONCAT('%', :recherche, '%'))
            ORDER BY ls.station.nom
            """)
    Page<Station> rechercherStationsDeLaLigne(Integer idLigne, String recherche, Pageable pagination);
}
