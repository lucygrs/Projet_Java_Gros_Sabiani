package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Profil;
import fr.efrei.idflm.modele.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UtilisateurRepositoryTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void motDePasseDuCompteDeDemonstrationEstStockeHache() {
        String motDePasseEnClair = "Responsable2026!";

        Utilisateur responsable = utilisateurRepository.findByIdentifiant("aubin.sabiani").orElseThrow();

        assertThat(responsable.getMotDePasse()).isNotEqualTo(motDePasseEnClair);
        assertThat(new BCryptPasswordEncoder().matches(motDePasseEnClair, responsable.getMotDePasse())).isTrue();
    }

    @Test
    void chaqueProfilPossedeUnCompteDeDemonstration() {
        assertThat(utilisateurRepository.countByProfil(Profil.RESPONSABLE_EXPLOITATION)).isEqualTo(1);
        assertThat(utilisateurRepository.countByProfil(Profil.AGENT_SIGNALEMENT)).isEqualTo(1);
    }
}
