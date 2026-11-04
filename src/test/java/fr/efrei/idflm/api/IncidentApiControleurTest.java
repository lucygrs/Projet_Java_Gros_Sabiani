package fr.efrei.idflm.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IncidentApiControleurTest {

    private static final String RESPONSABLE = "aubin.sabiani:Responsable2026!";
    private static final String AGENT = "lucy.gros:Agent2026!";
    private static final int ID_INCIDENT_NON_AFFECTE = 2;
    private static final int ID_INCIDENT_AFFECTE = 7;
    private static final int ID_INCIDENT_RESOLU = 9;
    private static final int ID_INCIDENT_RESOLU_LE_PLUS_RECENT = 11;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requeteSansAuthentificationEstRefuseeAvec401() throws Exception {
        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void incidentInexistantRenvoie404AvecUnMessageEnFrancais() throws Exception {
        mockMvc.perform(en(AGENT, get("/api/incidents/999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statut").value(404))
                .andExpect(jsonPath("$.message").value("L'incident 999 n'existe pas"));
    }

    @Test
    void incidentsActifsNeContiennentAucunIncidentResolu() throws Exception {
        mockMvc.perform(en(AGENT, get("/api/incidents")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].statut", everyItem(not("RESOLU"))));
    }

    @Test
    void historiqueCommenceParLIncidentResoluLePlusRecemment() throws Exception {
        mockMvc.perform(en(RESPONSABLE, get("/api/incidents/resolus")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID_INCIDENT_RESOLU_LE_PLUS_RECENT));
    }

    @Test
    void agentNePeutPasConsulterLHistorique() throws Exception {
        mockMvc.perform(en(AGENT, get("/api/incidents/resolus")))
                .andExpect(status().isForbidden());
    }

    @Test
    void responsableAffecteUnPrestataire() throws Exception {
        mockMvc.perform(en(RESPONSABLE, put("/api/incidents/" + ID_INCIDENT_NON_AFFECTE + "/prestataire"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPrestataire\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("AFFECTE"))
                .andExpect(jsonPath("$.niveauAttention").value("PRIS_EN_CHARGE"));
    }

    @Test
    void agentNePeutPasAffecterUnPrestataire() throws Exception {
        mockMvc.perform(en(AGENT, put("/api/incidents/" + ID_INCIDENT_NON_AFFECTE + "/prestataire"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idPrestataire\": 1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void resoudreUnIncidentAffecteLeFaitPasserEnResolu() throws Exception {
        mockMvc.perform(en(AGENT, put("/api/incidents/" + ID_INCIDENT_AFFECTE + "/resolution"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\": 4, \"commentaire\": \"Rapide\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RESOLU"));
    }

    @Test
    void resoudreUnIncidentDejaResoluRenvoie409() throws Exception {
        mockMvc.perform(en(RESPONSABLE, put("/api/incidents/" + ID_INCIDENT_RESOLU + "/resolution"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\": 4}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(endsWith("ne peut plus être résolu")));
    }

    @Test
    void noteHorsDeLIntervalleRenvoie400() throws Exception {
        mockMvc.perform(en(RESPONSABLE, put("/api/incidents/" + ID_INCIDENT_AFFECTE + "/resolution"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\": 6}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("La note doit être comprise entre 1 et 5"));
    }

    @Test
    void declarerUnIncidentRenvoie201AvecSonAdresse() throws Exception {
        mockMvc.perform(en(AGENT, post("/api/incidents"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEquipement\": 9, \"titre\": \"Porte bloquée\", \"description\": \"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/api/incidents/")))
                .andExpect(jsonPath("$.statut").value("NON_AFFECTE"))
                .andExpect(jsonPath("$.niveauAttention").value("VERT"));
    }

    private MockHttpServletRequestBuilder en(String identifiants, MockHttpServletRequestBuilder requete) {
        String encodage = Base64.getEncoder().encodeToString(identifiants.getBytes(StandardCharsets.UTF_8));
        return requete.header(HttpHeaders.AUTHORIZATION, "Basic " + encodage);
    }
}
