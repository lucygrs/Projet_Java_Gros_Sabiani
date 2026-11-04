package fr.efrei.idflm.modele;

import fr.efrei.idflm.exception.ResolutionImpossibleException;
import fr.efrei.idflm.exception.TransitionInterditeException;
import jakarta.persistence.*;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incident", nullable = false)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_equipement", nullable = false)
    private Equipement equipement;

    @Column(name = "titre", nullable = false, length = 100)
    private String titre;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "date_ouverture", nullable = false)
    private LocalDateTime dateOuverture;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutIncident statut;

    @ManyToOne
    @JoinColumn(name = "id_prestataire")
    private Prestataire prestataire;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    protected Incident() {
    }

    public static Incident declarer(Equipement equipement, String titre, String description,
                                    LocalDateTime dateOuverture) {
        Incident incident = new Incident();
        incident.equipement = equipement;
        incident.titre = titre;
        incident.description = description;
        incident.dateOuverture = dateOuverture;
        incident.statut = StatutIncident.NON_AFFECTE;
        return incident;
    }

    public void modifierInformations(String titre, String description) {
        verifierNonResolu("modifié");
        this.titre = titre;
        this.description = description;
    }

    public void affecter(Prestataire prestataire) {
        verifierNonResolu("affecté");
        this.prestataire = prestataire;
        this.statut = StatutIncident.AFFECTE;
    }

    public void resoudre(LocalDateTime dateResolution) {
        verifierNonResolu("résolu");
        if (!aUnPrestataire()) {
            throw new ResolutionImpossibleException(id, "aucun prestataire ne lui est affecté");
        }
        if (dateResolution.isBefore(dateOuverture)) {
            throw new ResolutionImpossibleException(id, "la date de résolution précède son ouverture");
        }
        this.dateResolution = dateResolution;
        this.statut = StatutIncident.RESOLU;
    }

    public boolean estResolu() {
        return statut == StatutIncident.RESOLU;
    }

    public boolean aUnPrestataire() {
        return prestataire != null;
    }

    public Duration dureeOuverture(Clock horloge) {
        LocalDateTime fin = estResolu() ? dateResolution : LocalDateTime.now(horloge);
        return Duration.between(dateOuverture, fin);
    }

    private void verifierNonResolu(String operationRefusee) {
        if (estResolu()) {
            throw new TransitionInterditeException(id, operationRefusee);
        }
    }

    public Integer getId() {
        return id;
    }

    public Equipement getEquipement() {
        return equipement;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateOuverture() {
        return dateOuverture;
    }

    public StatutIncident getStatut() {
        return statut;
    }

    public Prestataire getPrestataire() {
        return prestataire;
    }

    public LocalDateTime getDateResolution() {
        return dateResolution;
    }
}
