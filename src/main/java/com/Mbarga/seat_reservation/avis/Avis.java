package com.mbarga.seat_reservation.avis;

import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.trajet.Trajet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "avis")
@Getter @Setter @NoArgsConstructor
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passager_id", nullable = false)
    private User passager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id", nullable = false)
    private User chauffeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

    @Column(nullable = false)
    private int note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_avis", nullable = false)
    private OffsetDateTime dateAvis = OffsetDateTime.now();

    public Avis(User passager, User chauffeur, Trajet trajet, int note, String commentaire) {
        this.passager     = passager;
        this.chauffeur    = chauffeur;
        this.trajet       = trajet;
        this.note         = note;
        this.commentaire  = commentaire;
    }
}
