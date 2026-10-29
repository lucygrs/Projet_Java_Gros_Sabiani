package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "station_utilisateur")
public class StationUtilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_station_utilisateur", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_station", nullable = false)
    private Station station;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

}
