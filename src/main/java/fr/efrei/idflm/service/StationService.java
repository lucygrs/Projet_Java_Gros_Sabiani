package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.StationFormulaire;
import fr.efrei.idflm.exception.StationIntrouvableException;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.modele.Ligne;
import fr.efrei.idflm.modele.LigneStation;
import fr.efrei.idflm.modele.Station;
import fr.efrei.idflm.repository.EquipementRepository;
import fr.efrei.idflm.repository.LigneStationRepository;
import fr.efrei.idflm.repository.StationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;
    private final LigneStationRepository ligneStationRepository;
    private final EquipementRepository equipementRepository;
    private final LigneService ligneService;

    public StationService(StationRepository stationRepository, LigneStationRepository ligneStationRepository,
                          EquipementRepository equipementRepository, LigneService ligneService) {
        this.stationRepository = stationRepository;
        this.ligneStationRepository = ligneStationRepository;
        this.equipementRepository = equipementRepository;
        this.ligneService = ligneService;
    }

    public Station trouverStation(Integer idStation) {
        return stationRepository.findById(idStation)
                .orElseThrow(() -> new StationIntrouvableException(idStation));
    }

    public List<Station> listerStations() {
        return stationRepository.findAllByOrderByNomAsc();
    }

    public Page<Station> rechercherStationsDeLaLigne(Integer idLigne, String recherche, Pageable pagination) {
        return stationRepository.rechercherStationsDeLaLigne(idLigne, recherche, pagination);
    }

    // Une station déjà desservie par une autre ligne est réutilisée au lieu d'être dupliquée
    @Transactional
    public Station ajouterStationALaLigne(Integer idLigne, StationFormulaire formulaire) {
        Ligne ligne = ligneService.trouverLigne(idLigne);
        Station station = stationRepository.findByNomIgnoreCase(formulaire.nom())
                .orElseGet(() -> creerStation(formulaire.nom()));
        if (ligneStationRepository.findByLigneIdAndStationId(idLigne, station.getId()).isEmpty()) {
            LigneStation desserte = new LigneStation();
            desserte.setLigne(ligne);
            desserte.setStation(station);
            ligneStationRepository.save(desserte);
        }
        return station;
    }

    @Transactional
    public Station renommerStation(Integer idStation, StationFormulaire formulaire) {
        Station station = trouverStation(idStation);
        station.setNom(formulaire.nom());
        return station;
    }

    // La station disparaît avec sa dernière ligne, sauf si des équipements y sont encore installés
    @Transactional
    public void retirerStationDeLaLigne(Integer idLigne, Integer idStation) {
        Station station = trouverStation(idStation);
        ligneStationRepository.findByLigneIdAndStationId(idLigne, idStation)
                .ifPresent(ligneStationRepository::delete);
        if (ligneStationRepository.existsByStationId(idStation)) {
            return;
        }
        if (equipementRepository.existsByStationId(idStation)) {
            throw new SuppressionImpossibleException("la station " + station.getNom() + " a encore des équipements");
        }
        stationRepository.delete(station);
    }

    private Station creerStation(String nom) {
        Station station = new Station();
        station.setNom(nom);
        return stationRepository.save(station);
    }
}
