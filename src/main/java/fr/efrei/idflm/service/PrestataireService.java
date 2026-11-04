package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.PrestataireFormulaire;
import fr.efrei.idflm.exception.PrestataireIntrouvableException;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.modele.Prestataire;
import fr.efrei.idflm.repository.IncidentRepository;
import fr.efrei.idflm.repository.PrestataireRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrestataireService {

    private final PrestataireRepository prestataireRepository;
    private final IncidentRepository incidentRepository;

    public PrestataireService(PrestataireRepository prestataireRepository, IncidentRepository incidentRepository) {
        this.prestataireRepository = prestataireRepository;
        this.incidentRepository = incidentRepository;
    }

    public Prestataire trouverPrestataire(Integer idPrestataire) {
        return prestataireRepository.findById(idPrestataire)
                .orElseThrow(() -> new PrestataireIntrouvableException(idPrestataire));
    }

    public List<Prestataire> listerPrestataires() {
        return prestataireRepository.findAllByOrderByNomAsc();
    }

    public Page<Prestataire> rechercherPrestataires(String recherche, Pageable pagination) {
        return prestataireRepository.findByNomContainingIgnoreCase(recherche, pagination);
    }

    @Transactional
    public Prestataire creerPrestataire(PrestataireFormulaire formulaire) {
        Prestataire prestataire = new Prestataire();
        appliquerFormulaire(prestataire, formulaire);
        return prestataireRepository.save(prestataire);
    }

    @Transactional
    public Prestataire modifierPrestataire(Integer idPrestataire, PrestataireFormulaire formulaire) {
        Prestataire prestataire = trouverPrestataire(idPrestataire);
        appliquerFormulaire(prestataire, formulaire);
        return prestataire;
    }

    @Transactional
    public void supprimerPrestataire(Integer idPrestataire) {
        Prestataire prestataire = trouverPrestataire(idPrestataire);
        if (incidentRepository.existsByPrestataireId(idPrestataire)) {
            throw new SuppressionImpossibleException(
                    "le prestataire " + prestataire.getNom() + " est affecté à des incidents");
        }
        prestataireRepository.delete(prestataire);
    }

    private void appliquerFormulaire(Prestataire prestataire, PrestataireFormulaire formulaire) {
        prestataire.setNom(formulaire.nom());
        prestataire.setContact(formulaire.contact());
    }
}
