package fr.efrei.idflm.service;

import fr.efrei.idflm.modele.Utilisateur;
import fr.efrei.idflm.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ConnexionService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public ConnexionService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifiant) {
        Utilisateur utilisateur = utilisateurRepository.findByIdentifiant(identifiant)
                .orElseThrow(() -> new UsernameNotFoundException("Identifiant inconnu : " + identifiant));
        return User.withUsername(utilisateur.getIdentifiant())
                .password(utilisateur.getMotDePasse())
                .roles(utilisateur.getProfil().name())
                .build();
    }
}
