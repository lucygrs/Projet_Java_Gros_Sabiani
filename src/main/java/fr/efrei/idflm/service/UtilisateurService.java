package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.InformationsPersonnellesFormulaire;
import fr.efrei.idflm.dto.MotDePasseFormulaire;
import fr.efrei.idflm.dto.UtilisateurFormulaire;
import fr.efrei.idflm.exception.IdentifiantDejaUtiliseException;
import fr.efrei.idflm.exception.MotDePasseObligatoireException;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.exception.UtilisateurIntrouvableException;
import fr.efrei.idflm.modele.Utilisateur;
import fr.efrei.idflm.repository.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder encodeurMotDePasse;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder encodeurMotDePasse) {
        this.utilisateurRepository = utilisateurRepository;
        this.encodeurMotDePasse = encodeurMotDePasse;
    }

    public Utilisateur trouverUtilisateur(Integer idUtilisateur) {
        return utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new UtilisateurIntrouvableException(idUtilisateur));
    }

    public Utilisateur trouverParIdentifiant(String identifiant) {
        return utilisateurRepository.findByIdentifiant(identifiant)
                .orElseThrow(() -> new UtilisateurIntrouvableException(identifiant));
    }

    public Page<Utilisateur> rechercherUtilisateurs(String recherche, Pageable pagination) {
        return utilisateurRepository.rechercherUtilisateurs(recherche, pagination);
    }

    @Transactional
    public Utilisateur creerUtilisateur(UtilisateurFormulaire formulaire) {
        if (!motDePasseRenseigne(formulaire)) {
            throw new MotDePasseObligatoireException();
        }
        if (utilisateurRepository.existsByIdentifiant(formulaire.identifiant())) {
            throw new IdentifiantDejaUtiliseException(formulaire.identifiant());
        }
        Utilisateur utilisateur = new Utilisateur();
        appliquerFormulaire(utilisateur, formulaire);
        return utilisateurRepository.save(utilisateur);
    }

    // Le mot de passe n'est remplacé que s'il est saisi, sinon l'ancien est conservé
    @Transactional
    public Utilisateur modifierUtilisateur(Integer idUtilisateur, UtilisateurFormulaire formulaire) {
        Utilisateur utilisateur = trouverUtilisateur(idUtilisateur);
        verifierIdentifiantDisponible(utilisateur, formulaire.identifiant());
        appliquerFormulaire(utilisateur, formulaire);
        return utilisateur;
    }

    @Transactional
    public Utilisateur modifierInformationsPersonnelles(String identifiantActuel,
                                                        InformationsPersonnellesFormulaire formulaire) {
        Utilisateur utilisateur = trouverParIdentifiant(identifiantActuel);
        verifierIdentifiantDisponible(utilisateur, formulaire.identifiant());
        utilisateur.setIdentifiant(formulaire.identifiant());
        utilisateur.setNom(formulaire.nom());
        utilisateur.setPrenom(formulaire.prenom());
        return utilisateur;
    }

    @Transactional
    public void changerMotDePasse(String identifiant, MotDePasseFormulaire formulaire) {
        Utilisateur utilisateur = trouverParIdentifiant(identifiant);
        utilisateur.setMotDePasse(encodeurMotDePasse.encode(formulaire.nouveauMotDePasse()));
    }

    @Transactional
    public void supprimerUtilisateur(Integer idUtilisateur, String identifiantConnecte) {
        Utilisateur utilisateur = trouverUtilisateur(idUtilisateur);
        if (utilisateur.getIdentifiant().equals(identifiantConnecte)) {
            throw new SuppressionImpossibleException("vous ne pouvez pas supprimer votre propre compte");
        }
        utilisateurRepository.delete(utilisateur);
    }

    private void appliquerFormulaire(Utilisateur utilisateur, UtilisateurFormulaire formulaire) {
        utilisateur.setIdentifiant(formulaire.identifiant());
        utilisateur.setNom(formulaire.nom());
        utilisateur.setPrenom(formulaire.prenom());
        utilisateur.setProfil(formulaire.profil());
        if (motDePasseRenseigne(formulaire)) {
            utilisateur.setMotDePasse(encodeurMotDePasse.encode(formulaire.motDePasse()));
        }
    }

    private void verifierIdentifiantDisponible(Utilisateur utilisateur, String identifiantSouhaite) {
        boolean identifiantChange = !utilisateur.getIdentifiant().equals(identifiantSouhaite);
        if (identifiantChange && utilisateurRepository.existsByIdentifiant(identifiantSouhaite)) {
            throw new IdentifiantDejaUtiliseException(identifiantSouhaite);
        }
    }

    private boolean motDePasseRenseigne(UtilisateurFormulaire formulaire) {
        return formulaire.motDePasse() != null && !formulaire.motDePasse().isBlank();
    }
}
