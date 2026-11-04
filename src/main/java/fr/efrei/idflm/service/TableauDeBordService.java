package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.IndicateursTableauDeBord;
import fr.efrei.idflm.modele.Profil;
import fr.efrei.idflm.modele.StatutIncident;
import fr.efrei.idflm.repository.EquipementRepository;
import fr.efrei.idflm.repository.IncidentRepository;
import fr.efrei.idflm.repository.LigneRepository;
import fr.efrei.idflm.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

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
}
