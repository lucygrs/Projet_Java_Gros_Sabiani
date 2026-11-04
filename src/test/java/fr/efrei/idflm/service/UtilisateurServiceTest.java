package fr.efrei.idflm.service;

import fr.efrei.idflm.dto.InformationsPersonnellesFormulaire;
import fr.efrei.idflm.dto.UtilisateurFormulaire;
import fr.efrei.idflm.exception.IdentifiantDejaUtiliseException;
import fr.efrei.idflm.exception.MotDePasseObligatoireException;
import fr.efrei.idflm.exception.SuppressionImpossibleException;
import fr.efrei.idflm.modele.Profil;
import fr.efrei.idflm.modele.Utilisateur;
import fr.efrei.idflm.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    private static final String MOT_DE_PASSE = "Agent2026!";

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private final PasswordEncoder encodeurMotDePasse = new BCryptPasswordEncoder();
    private UtilisateurService utilisateurService;

    @BeforeEach
    void preparer() {
        utilisateurService = new UtilisateurService(utilisateurRepository, encodeurMotDePasse);
    }

    @Test
    void creerUnUtilisateurStockeSonMotDePasseHache() {
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(appel -> appel.getArgument(0));

        Utilisateur utilisateur = utilisateurService.creerUtilisateur(formulaire("lucy.gros", MOT_DE_PASSE));

        assertThat(utilisateur.getMotDePasse()).isNotEqualTo(MOT_DE_PASSE);
        assertThat(encodeurMotDePasse.matches(MOT_DE_PASSE, utilisateur.getMotDePasse())).isTrue();
    }

    @Test
    void creerUnUtilisateurSansMotDePasseEstRefuse() {
        assertThatThrownBy(() -> utilisateurService.creerUtilisateur(formulaire("lucy.gros", " ")))
                .isInstanceOf(MotDePasseObligatoireException.class);
    }

    @Test
    void creerUnUtilisateurAvecUnIdentifiantDejaUtiliseEstRefuse() {
        when(utilisateurRepository.existsByIdentifiant("lucy.gros")).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.creerUtilisateur(formulaire("lucy.gros", MOT_DE_PASSE)))
                .isInstanceOf(IdentifiantDejaUtiliseException.class);
    }

    @Test
    void modifierSesInformationsAvecUnIdentifiantDejaPrisEstRefuse() {
        Utilisateur connecte = new Utilisateur();
        connecte.setIdentifiant("lucy.gros");
        when(utilisateurRepository.findByIdentifiant("lucy.gros")).thenReturn(Optional.of(connecte));
        when(utilisateurRepository.existsByIdentifiant("aubin.sabiani")).thenReturn(true);
        InformationsPersonnellesFormulaire formulaire =
                new InformationsPersonnellesFormulaire("aubin.sabiani", "Gros", "Lucy");

        assertThatThrownBy(() -> utilisateurService.modifierInformationsPersonnelles("lucy.gros", formulaire))
                .isInstanceOf(IdentifiantDejaUtiliseException.class);
    }

    @Test
    void modifierSesInformationsChangeLIdentifiantDeConnexion() {
        Utilisateur connecte = new Utilisateur();
        connecte.setIdentifiant("lucy.gros");
        when(utilisateurRepository.findByIdentifiant("lucy.gros")).thenReturn(Optional.of(connecte));

        utilisateurService.modifierInformationsPersonnelles("lucy.gros",
                new InformationsPersonnellesFormulaire("l.gros", "Gros", "Lucie"));

        assertThat(connecte.getIdentifiant()).isEqualTo("l.gros");
        assertThat(connecte.getPrenom()).isEqualTo("Lucie");
    }

    @Test
    void supprimerSonPropreCompteEstRefuse() {
        Utilisateur connecte = new Utilisateur();
        connecte.setIdentifiant("aubin.sabiani");
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(connecte));

        assertThatThrownBy(() -> utilisateurService.supprimerUtilisateur(1, "aubin.sabiani"))
                .isInstanceOf(SuppressionImpossibleException.class);
    }

    private UtilisateurFormulaire formulaire(String identifiant, String motDePasse) {
        return new UtilisateurFormulaire(identifiant, "Gros", "Lucy", motDePasse, Profil.AGENT_SIGNALEMENT);
    }
}
