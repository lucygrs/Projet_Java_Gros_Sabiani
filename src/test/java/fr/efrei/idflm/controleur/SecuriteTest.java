package fr.efrei.idflm.controleur;

import fr.efrei.idflm.modele.Profil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecuriteTest {

    private static final String RESPONSABLE = "aubin.sabiani";
    private static final String AGENT = "lucy.gros";
    private static final Pattern JETON_CSRF = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void utilisateurNonConnecteEstRedirigeVersLaConnexion() throws Exception {
        mockMvc.perform(get("/incidents"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/connexion"));
    }

    @Test
    void mauvaisMotDePasseReafficheLaConnexionAvecUnMessage() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/connexion").session(session)
                        .param("identifiant", AGENT)
                        .param("motDePasse", "mauvais")
                        .param("_csrf", jetonCsrf(session, "/connexion")))
                .andExpect(redirectedUrl("/connexion?erreur"));
        mockMvc.perform(get("/connexion").param("erreur", ""))
                .andExpect(content().string(containsString("Identifiant ou mot de passe incorrect")));
    }

    @Test
    void bonsIdentifiantsOuvrentLaSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/connexion").session(session)
                        .param("identifiant", AGENT)
                        .param("motDePasse", "Agent2026!")
                        .param("_csrf", jetonCsrf(session, "/connexion")))
                .andExpect(redirectedUrl("/"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/tableau-de-bord", "/incidents/passes", "/incidents/2/affectation",
            "/incidents/2/modification", "/equipements/nouveau", "/lignes/nouvelle", "/utilisateurs",
            "/prestataires"})
    void agentNAccedePasAuxPagesReserveesAuResponsable(String page) throws Exception {
        mockMvc.perform(get(page).session(sessionDe(AGENT, Profil.AGENT_SIGNALEMENT)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/tableau-de-bord", "/incidents/passes", "/incidents/2/affectation",
            "/incidents/2/modification", "/equipements/nouveau", "/lignes/nouvelle", "/utilisateurs",
            "/prestataires"})
    void responsableAccedeAuxPagesQuiLuiSontReservees(String page) throws Exception {
        mockMvc.perform(get(page).session(sessionDe(RESPONSABLE, Profil.RESPONSABLE_EXPLOITATION)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/incidents", "/incidents/nouveau", "/incidents/2", "/incidents/7/resolution",
            "/equipements", "/equipements/1/fiche", "/lignes", "/lignes/1/stations", "/parametres"})
    void agentAccedeAuxFonctionnalitesDeSonProfil(String page) throws Exception {
        mockMvc.perform(get(page).session(sessionDe(AGENT, Profil.AGENT_SIGNALEMENT)))
                .andExpect(status().isOk());
    }

    @Test
    void agentNePeutPasSupprimerUnIncidentMemeEnForcantLaRequete() throws Exception {
        MockHttpSession session = sessionDe(AGENT, Profil.AGENT_SIGNALEMENT);

        mockMvc.perform(post("/incidents/2").session(session)
                        .param("_method", "DELETE")
                        .param("_csrf", jetonCsrf(session, "/incidents/nouveau")))
                .andExpect(status().isForbidden());
    }

    @Test
    void incidentInexistantAfficheUnePage404SansTraceTechnique() throws Exception {
        String page = mockMvc.perform(get("/incidents/999")
                        .session(sessionDe(RESPONSABLE, Profil.RESPONSABLE_EXPLOITATION)))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        assertThat(page).contains("incident 999 n").doesNotContain("Exception");
    }

    private MockHttpSession sessionDe(String identifiant, Profil profil) {
        SecurityContext contexte = SecurityContextHolder.createEmptyContext();
        contexte.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                identifiant, null, List.of(new SimpleGrantedAuthority("ROLE_" + profil.name()))));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, contexte);
        return session;
    }

    private String jetonCsrf(MockHttpSession session, String pageAvecFormulaire) throws Exception {
        String page = mockMvc.perform(get(pageAvecFormulaire).session(session))
                .andReturn().getResponse().getContentAsString();
        Matcher jeton = JETON_CSRF.matcher(page);
        assertThat(jeton.find()).isTrue();
        return jeton.group(1);
    }
}
