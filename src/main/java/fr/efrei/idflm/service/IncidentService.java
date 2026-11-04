package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.DeclarationIncidentFormulaire;
import fr.efrei.idflm.dto.IncidentSuivi;
import fr.efrei.idflm.dto.ModificationIncidentFormulaire;
import fr.efrei.idflm.dto.ResolutionFormulaire;
import fr.efrei.idflm.exception.EquipementIntrouvableException;
import fr.efrei.idflm.exception.IncidentIntrouvableException;
import fr.efrei.idflm.exception.PrestataireIntrouvableException;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.modele.Equipement;
import fr.efrei.idflm.modele.Evaluation;
import fr.efrei.idflm.modele.Incident;
import fr.efrei.idflm.modele.Prestataire;
import fr.efrei.idflm.modele.StatutIncident;
import fr.efrei.idflm.repository.EquipementRepository;
import fr.efrei.idflm.repository.EvaluationRepository;
import fr.efrei.idflm.repository.IncidentRepository;
import fr.efrei.idflm.repository.PrestataireRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final EquipementRepository equipementRepository;
    private final PrestataireRepository prestataireRepository;
    private final EvaluationRepository evaluationRepository;
    private final PolitiqueUrgence politiqueUrgence;
    private final Clock horloge;

    public IncidentService(IncidentRepository incidentRepository, EquipementRepository equipementRepository,
                           PrestataireRepository prestataireRepository, EvaluationRepository evaluationRepository,
                           PolitiqueUrgence politiqueUrgence, Clock horloge) {
        this.incidentRepository = incidentRepository;
        this.equipementRepository = equipementRepository;
        this.prestataireRepository = prestataireRepository;
        this.evaluationRepository = evaluationRepository;
        this.politiqueUrgence = politiqueUrgence;
        this.horloge = horloge;
    }

    public Incident trouverIncident(Integer idIncident) {
        return incidentRepository.findById(idIncident)
                .orElseThrow(() -> new IncidentIntrouvableException(idIncident));
    }

    public Page<IncidentSuivi> rechercherIncidentsActifs(Integer idStation, StatutIncident statut, String recherche,
                                                         Pageable pagination) {
        return incidentRepository.rechercherIncidentsActifs(idStation, statut, recherche, pagination)
                .map(this::suivreIncident);
    }

    public List<IncidentSuivi> listerSignalementsEnCours() {
        return incidentRepository.findByStatutNotOrderByDateOuvertureAsc(StatutIncident.RESOLU).stream()
                .map(this::suivreIncident)
                .toList();
    }

    public List<IncidentSuivi> listerIncidentsResolus() {
        return incidentRepository.rechercherIncidentsResolus("", Pageable.unpaged()).stream()
                .map(this::suivreIncident)
                .toList();
    }

    public IncidentSuivi suivreIncident(Incident incident) {
        return new IncidentSuivi(incident, politiqueUrgence.evaluer(incident), incident.dureeOuverture(horloge));
    }

    public Page<Incident> rechercherHistorique(String recherche, Pageable pagination) {
        return incidentRepository.rechercherIncidentsResolus(recherche, pagination);
    }

    public List<Incident> listerIncidentsDeLEquipement(Integer idEquipement) {
        return incidentRepository.findByEquipementIdOrderByDateOuvertureDesc(idEquipement);
    }

    public Optional<Evaluation> trouverEvaluation(Integer idIncident) {
        return evaluationRepository.findByIncidentId(idIncident);
    }

    public Map<Integer, Evaluation> trouverEvaluations(List<Incident> incidents) {
        List<Integer> idsIncidents = incidents.stream().map(Incident::getId).toList();
        return evaluationRepository.findByIncidentIdIn(idsIncidents).stream()
                .collect(Collectors.toMap(evaluation -> evaluation.getIncident().getId(), Function.identity()));
    }

    // L'incident et l'état de son équipement changent ensemble ou pas du tout (RG10)
    @Transactional
    public Incident declarerIncident(DeclarationIncidentFormulaire formulaire) {
        Equipement equipement = equipementRepository.findById(formulaire.idEquipement())
                .orElseThrow(() -> new EquipementIntrouvableException(formulaire.idEquipement()));
        Incident incident = Incident.declarer(equipement, formulaire.titre(), formulaire.description(),
                LocalDateTime.now(horloge));
        equipement.signalerPanne();
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident modifierIncident(Integer idIncident, ModificationIncidentFormulaire formulaire) {
        Incident incident = trouverIncident(idIncident);
        incident.modifierInformations(formulaire.titre(), formulaire.description());
        return incident;
    }

    @Transactional
    public Incident affecterPrestataire(Integer idIncident, Integer idPrestataire) {
        Incident incident = trouverIncident(idIncident);
        Prestataire prestataire = prestataireRepository.findById(idPrestataire)
                .orElseThrow(() -> new PrestataireIntrouvableException(idPrestataire));
        incident.affecter(prestataire);
        return incident;
    }

    // Résolution, évaluation et remise en service sont enregistrées ensemble ou pas du tout (RG10)
    @Transactional
    public Incident resoudreIncident(Integer idIncident, ResolutionFormulaire formulaire) {
        Incident incident = trouverIncident(idIncident);
        LocalDateTime maintenant = LocalDateTime.now(horloge);
        incident.resoudre(maintenant);
        evaluationRepository.save(new Evaluation(incident, formulaire.note(), formulaire.commentaire(), maintenant));
        remettreEnServiceSiPlusAucunIncidentActif(incident);
        return incident;
    }

    @Transactional
    public void supprimerIncident(Integer idIncident) {
        Incident incident = trouverIncident(idIncident);
        if (incident.estResolu()) {
            throw new SuppressionImpossibleException(
                    "l'incident " + idIncident + " est résolu et doit rester dans l'historique");
        }
        remettreEnServiceSiPlusAucunIncidentActif(incident);
        incidentRepository.delete(incident);
    }

    private void remettreEnServiceSiPlusAucunIncidentActif(Incident incident) {
        Equipement equipement = incident.getEquipement();
        boolean autreIncidentActif = incidentRepository.existsByEquipementIdAndStatutNotAndIdNot(
                equipement.getId(), StatutIncident.RESOLU, incident.getId());
        if (!autreIncidentActif) {
            equipement.remettreEnService();
        }
    }
}
