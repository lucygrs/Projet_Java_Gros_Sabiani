package fr.efrei.idflm.controleur;

import fr.efrei.idflm.dto.LigneFormulaire;
import fr.efrei.idflm.dto.StationFormulaire;
import fr.efrei.idflm.modele.Ligne;
import fr.efrei.idflm.modele.Station;
import fr.efrei.idflm.service.LigneService;
import fr.efrei.idflm.service.StationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/lignes")
public class LigneControleur {

    private static final String REDIRECTION_LIGNES = "redirect:/lignes";
    private static final String POPUP_LIGNE = "popups/formulaire-ligne";
    private static final String POPUP_STATION = "popups/formulaire-station";
    private static final String COULEUR_PAR_DEFAUT = "#507B3E";

    private final LigneService ligneService;
    private final StationService stationService;

    public LigneControleur(LigneService ligneService, StationService stationService) {
        this.ligneService = ligneService;
        this.stationService = stationService;
    }

    @GetMapping
    public String afficherLignes(@RequestParam(defaultValue = "") String recherche,
                                 @RequestParam(defaultValue = "0") int page,
                                 HttpServletRequest requete, Model modele) {
        Page<Ligne> lignes = ligneService.rechercherLignes(recherche, Pagination.demanderPage(page));
        Map<Integer, Long> nombreStations = lignes.getContent().stream()
                .collect(Collectors.toMap(Ligne::getId, ligne -> ligneService.compterStationsDeLaLigne(ligne.getId())));
        modele.addAttribute("lignes", lignes.getContent());
        modele.addAttribute("nombreStations", nombreStations);
        modele.addAttribute("pagination", Pagination.decrire(lignes, requete));
        modele.addAttribute("recherche", recherche);
        return "lignes";
    }

    @GetMapping("/nouvelle")
    public String afficherCreation(Model modele) {
        modele.addAttribute("formulaire", new LigneFormulaire("", "", COULEUR_PAR_DEFAUT));
        modele.addAttribute("idLigne", null);
        return POPUP_LIGNE;
    }

    @PostMapping
    public String creerLigne(@Valid @ModelAttribute("formulaire") LigneFormulaire formulaire,
                             BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            modele.addAttribute("idLigne", null);
            return POPUP_LIGNE;
        }
        ligneService.creerLigne(formulaire);
        return REDIRECTION_LIGNES;
    }

    @GetMapping("/{idLigne}/modification")
    public String afficherModification(@PathVariable Integer idLigne, Model modele) {
        Ligne ligne = ligneService.trouverLigne(idLigne);
        modele.addAttribute("formulaire", new LigneFormulaire(ligne.getNom(), ligne.getImage(), ligne.getCouleur()));
        modele.addAttribute("idLigne", idLigne);
        return POPUP_LIGNE;
    }

    @PutMapping("/{idLigne}")
    public String modifierLigne(@PathVariable Integer idLigne,
                                @Valid @ModelAttribute("formulaire") LigneFormulaire formulaire,
                                BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            modele.addAttribute("idLigne", idLigne);
            return POPUP_LIGNE;
        }
        ligneService.modifierLigne(idLigne, formulaire);
        return REDIRECTION_LIGNES;
    }

    @DeleteMapping("/{idLigne}")
    public String supprimerLigne(@PathVariable Integer idLigne) {
        ligneService.supprimerLigne(idLigne);
        return REDIRECTION_LIGNES;
    }

    @GetMapping("/{idLigne}/stations")
    public String afficherStationsDeLaLigne(@PathVariable Integer idLigne,
                                            @RequestParam(defaultValue = "") String recherche,
                                            @RequestParam(defaultValue = "0") int page,
                                            HttpServletRequest requete, Model modele) {
        Page<Station> stations = stationService.rechercherStationsDeLaLigne(
                idLigne, recherche, Pagination.demanderPage(page));
        modele.addAttribute("ligne", ligneService.trouverLigne(idLigne));
        modele.addAttribute("stations", stations.getContent());
        modele.addAttribute("pagination", Pagination.decrire(stations, requete));
        modele.addAttribute("recherche", recherche);
        return "stations-ligne";
    }

    @GetMapping("/{idLigne}/stations/nouvelle")
    public String afficherAjoutStation(@PathVariable Integer idLigne, Model modele) {
        modele.addAttribute("formulaire", new StationFormulaire(""));
        return preparerFormulaireStation(idLigne, null, modele);
    }

    @PostMapping("/{idLigne}/stations")
    public String ajouterStation(@PathVariable Integer idLigne,
                                 @Valid @ModelAttribute("formulaire") StationFormulaire formulaire,
                                 BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            return preparerFormulaireStation(idLigne, null, modele);
        }
        stationService.ajouterStationALaLigne(idLigne, formulaire);
        return redirectionStations(idLigne);
    }

    @GetMapping("/{idLigne}/stations/{idStation}/modification")
    public String afficherModificationStation(@PathVariable Integer idLigne, @PathVariable Integer idStation,
                                              Model modele) {
        modele.addAttribute("formulaire", new StationFormulaire(stationService.trouverStation(idStation).getNom()));
        return preparerFormulaireStation(idLigne, idStation, modele);
    }

    @PutMapping("/{idLigne}/stations/{idStation}")
    public String renommerStation(@PathVariable Integer idLigne, @PathVariable Integer idStation,
                                  @Valid @ModelAttribute("formulaire") StationFormulaire formulaire,
                                  BindingResult resultat, HttpServletResponse reponse, Model modele) {
        if (resultat.hasErrors()) {
            ErreursFormulaire.signaler(resultat, reponse, modele);
            return preparerFormulaireStation(idLigne, idStation, modele);
        }
        stationService.renommerStation(idStation, formulaire);
        return redirectionStations(idLigne);
    }

    @DeleteMapping("/{idLigne}/stations/{idStation}")
    public String retirerStation(@PathVariable Integer idLigne, @PathVariable Integer idStation) {
        stationService.retirerStationDeLaLigne(idLigne, idStation);
        return redirectionStations(idLigne);
    }

    private String preparerFormulaireStation(Integer idLigne, Integer idStation, Model modele) {
        modele.addAttribute("ligne", ligneService.trouverLigne(idLigne));
        modele.addAttribute("idStation", idStation);
        return POPUP_STATION;
    }

    private String redirectionStations(Integer idLigne) {
        return "redirect:/lignes/" + idLigne + "/stations";
    }
}
