package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.modele.StatutIncident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {

    long countByStatutNot(StatutIncident statut);

    boolean existsByEquipementId(Integer idEquipement);

    boolean existsByPrestataireId(Integer idPrestataire);

    boolean existsByEquipementIdAndStatutNotAndIdNot(Integer idEquipement, StatutIncident statut, Integer idIncident);

    List<Incident> findByStatutNotOrderByDateOuvertureAsc(StatutIncident statut);

    List<Incident> findByEquipementIdOrderByDateOuvertureDesc(Integer idEquipement);

    @Query("""
            SELECT i FROM Incident i
            WHERE i.statut <> fr.efrei.idflm.modele.StatutIncident.RESOLU
            AND (:idStation IS NULL OR i.equipement.station.id = :idStation)
            AND (:statut IS NULL OR i.statut = :statut)
            AND (LOWER(i.titre) LIKE LOWER(CONCAT('%', :recherche, '%'))
                OR LOWER(i.equipement.nom) LIKE LOWER(CONCAT('%', :recherche, '%')))
            ORDER BY i.dateOuverture
            """)
    Page<Incident> rechercherIncidentsActifs(Integer idStation, StatutIncident statut, String recherche,
                                            Pageable pagination);

    @Query("""
            SELECT i FROM Incident i
            WHERE i.statut = fr.efrei.idflm.modele.StatutIncident.RESOLU
            AND (LOWER(i.titre) LIKE LOWER(CONCAT('%', :recherche, '%'))
                OR LOWER(i.equipement.nom) LIKE LOWER(CONCAT('%', :recherche, '%')))
            ORDER BY i.dateResolution DESC
            """)
    Page<Incident> rechercherIncidentsResolus(String recherche, Pageable pagination);
}
