package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.LigneFormulaire;
import fr.efrei.idflm.exception.LigneIntrouvableException;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.modele.Ligne;
import fr.efrei.idflm.repository.LigneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LigneService {

    private final LigneRepository ligneRepository;

    public LigneService(LigneRepository ligneRepository) {
        this.ligneRepository = ligneRepository;
    }

    public Ligne trouverLigne(Integer idLigne) {
        return ligneRepository.findById(idLigne)
                .orElseThrow(() -> new LigneIntrouvableException(idLigne));
    }

    public Page<Ligne> rechercherLignes(String recherche, Pageable pagination) {
        return ligneRepository.findByNomContainingIgnoreCase(recherche, pagination);
    }

    public long compterStationsDeLaLigne(Integer idLigne) {
        return ligneRepository.compterStationsDeLaLigne(idLigne);
    }

    @Transactional
    public Ligne creerLigne(LigneFormulaire formulaire) {
        Ligne ligne = new Ligne();
        appliquerFormulaire(ligne, formulaire);
        return ligneRepository.save(ligne);
    }

    @Transactional
    public Ligne modifierLigne(Integer idLigne, LigneFormulaire formulaire) {
        Ligne ligne = trouverLigne(idLigne);
        appliquerFormulaire(ligne, formulaire);
        return ligne;
    }

    @Transactional
    public void supprimerLigne(Integer idLigne) {
        Ligne ligne = trouverLigne(idLigne);
        if (compterStationsDeLaLigne(idLigne) > 0) {
            throw new SuppressionImpossibleException("la " + ligne.getNom() + " dessert encore des stations");
        }
        ligneRepository.delete(ligne);
    }

    private void appliquerFormulaire(Ligne ligne, LigneFormulaire formulaire) {
        ligne.setNom(formulaire.nom());
        ligne.setImage(formulaire.image());
        ligne.setCouleur(formulaire.couleur());
    }
}
