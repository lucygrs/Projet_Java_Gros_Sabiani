package altn72.tpfilrouge.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "ligne_station")
public class LigneStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne_station", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_ligne", nullable = false)
    private Ligne ligne;

    @ManyToOne
    @JoinColumn(name = "id_station", nullable = false)
    private Station station;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Ligne getLigne() {
        return ligne;
    }

    public void setLigne(Ligne ligne) {
        this.ligne = ligne;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

}
