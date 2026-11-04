package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.EquipementFormulaire;
import fr.efrei.idflm.exception.EquipementIntrouvableException;
import fr.efrei.idflm.exception.ReferenceDejaUtiliseeException;
import fr.efrei.idflm.exception.StationIntrouvableException;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.modele.Equipement;
import fr.efrei.idflm.modele.Station;
import fr.efrei.idflm.repository.EquipementRepository;
import fr.efrei.idflm.repository.IncidentRepository;
import fr.efrei.idflm.repository.StationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipementService {

    private final EquipementRepository equipementRepository;
    private final StationRepository stationRepository;
    private final IncidentRepository incidentRepository;

    public EquipementService(EquipementRepository equipementRepository, StationRepository stationRepository,
                             IncidentRepository incidentRepository) {
        this.equipementRepository = equipementRepository;
        this.stationRepository = stationRepository;
        this.incidentRepository = incidentRepository;
    }

    public Equipement trouverEquipement(Integer idEquipement) {
        return equipementRepository.findById(idEquipement)
                .orElseThrow(() -> new EquipementIntrouvableException(idEquipement));
    }

    public Page<Equipement> rechercherEquipements(Integer idStation, String recherche, Pageable pagination) {
        return equipementRepository.rechercherEquipements(idStation, recherche, pagination);
    }

    public List<Equipement> listerEquipements() {
        return equipementRepository.findAll(Sort.by("nom"));
    }

    @Transactional
    public Equipement creerEquipement(EquipementFormulaire formulaire) {
        if (equipementRepository.existsByReference(formulaire.reference())) {
            throw new ReferenceDejaUtiliseeException(formulaire.reference());
        }
        Equipement equipement = new Equipement();
        appliquerFormulaire(equipement, formulaire);
        return equipementRepository.save(equipement);
    }

    @Transactional
    public Equipement modifierEquipement(Integer idEquipement, EquipementFormulaire formulaire) {
        Equipement equipement = trouverEquipement(idEquipement);
        if (equipementRepository.existsByReferenceAndIdNot(formulaire.reference(), idEquipement)) {
            throw new ReferenceDejaUtiliseeException(formulaire.reference());
        }
        appliquerFormulaire(equipement, formulaire);
        return equipement;
    }

    @Transactional
    public void supprimerEquipement(Integer idEquipement) {
        Equipement equipement = trouverEquipement(idEquipement);
        if (incidentRepository.existsByEquipementId(idEquipement)) {
            throw new SuppressionImpossibleException("l'équipement " + equipement.getNom() + " a des incidents");
        }
        equipementRepository.delete(equipement);
    }

    private void appliquerFormulaire(Equipement equipement, EquipementFormulaire formulaire) {
        Station station = stationRepository.findById(formulaire.idStation())
                .orElseThrow(() -> new StationIntrouvableException(formulaire.idStation()));
        equipement.setReference(formulaire.reference());
        equipement.setNom(formulaire.nom());
        equipement.setDescription(formulaire.description());
        equipement.setType(formulaire.type());
        equipement.setStation(station);
        equipement.setEtat(formulaire.etat());
    }
}
