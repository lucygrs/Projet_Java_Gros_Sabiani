package fr.efrei.idflm.modele;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation")
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluation", nullable = false)
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_incident", nullable = false, unique = true)
    private Incident incident;

    @Column(name = "note", nullable = false)
    private Integer note;

    @Column(name = "commentaire", length = 500)
    private String commentaire;

    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation;

    protected Evaluation() {
    }

    public Evaluation(Incident incident, Integer note, String commentaire, LocalDateTime dateEvaluation) {
        this.incident = incident;
        this.note = note;
        this.commentaire = commentaire;
        this.dateEvaluation = dateEvaluation;
    }

    public Integer getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public Integer getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public LocalDateTime getDateEvaluation() {
        return dateEvaluation;
    }
}
