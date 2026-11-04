package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.IndicateursTableauDeBord;
import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.modele.Profil;
import fr.efrei.idflm.modele.StatutIncident;
import fr.efrei.idflm.repository.EquipementRepository;
import fr.efrei.idflm.repository.IncidentRepository;
import fr.efrei.idflm.repository.LigneRepository;
import fr.efrei.idflm.repository.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableauDeBordService {

    private final IncidentRepository incidentRepository;
    private final EquipementRepository equipementRepository;
    private final LigneRepository ligneRepository;
    private final UtilisateurRepository utilisateurRepository;

    public TableauDeBordService(IncidentRepository incidentRepository, EquipementRepository equipementRepository,
                                LigneRepository ligneRepository, UtilisateurRepository utilisateurRepository) {
        this.incidentRepository = incidentRepository;
        this.equipementRepository = equipementRepository;
        this.ligneRepository = ligneRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public IndicateursTableauDeBord calculerIndicateurs() {
        return new IndicateursTableauDeBord(
                incidentRepository.countByStatutNot(StatutIncident.RESOLU),
                equipementRepository.count(),
                ligneRepository.count(),
                utilisateurRepository.countByProfil(Profil.AGENT_SIGNALEMENT),
                utilisateurRepository.countByProfil(Profil.RESPONSABLE_EXPLOITATION));
    }

    public Page<Incident> rechercherIncidentsActifs(Integer idStation, StatutIncident statut, String recherche,
                                                    Pageable pagination) {
        return incidentRepository.rechercherIncidentsActifs(idStation, statut, recherche, pagination);
    }

    public List<Incident> listerSignalementsEnCours() {
        return incidentRepository.findByStatutNotOrderByDateOuvertureAsc(StatutIncident.RESOLU);
    }
}
