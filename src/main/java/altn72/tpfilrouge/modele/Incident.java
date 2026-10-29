package altn72.tpfilrouge.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incident", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_equipement", nullable = false)
    private Equipement equipement;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "date_incident", nullable = false)
    private LocalDateTime dateIncident;

    @Column(name = "statut", length = 100)
    private String statut;

    @ManyToOne
    @JoinColumn(name = "id_prestataire")
    private Prestataire prestataire;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Equipement getEquipement() {
        return equipement;
    }

    public void setEquipement(Equipement equipement) {
        this.equipement = equipement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateIncident() {
        return dateIncident;
    }

    public void setDateIncident(LocalDateTime dateIncident) {
        this.dateIncident = dateIncident;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Prestataire getPrestataire() {
        return prestataire;
    }

    public void setPrestataire(Prestataire prestataire) {
        this.prestataire = prestataire;
    }

    public LocalDateTime getDateResolution() {
        return dateResolution;
    }

    public void setDateResolution(LocalDateTime dateResolution) {
        this.dateResolution = dateResolution;
    }

}
