package com.mbarga.seat_reservation.trajet;

import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.vehicule.Vehicule;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trajet")
@Getter @Setter @NoArgsConstructor
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id", nullable = false)
    private User chauffeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @Column(name = "point_depart", nullable = false)
    private String pointDepart;

    @Column(name = "lat_depart", nullable = false, precision = 10, scale = 7)
    private BigDecimal latDepart;

    @Column(name = "lng_depart", nullable = false, precision = 10, scale = 7)
    private BigDecimal lngDepart;

    @Column(name = "point_arrivee", nullable = false)
    private String pointArrivee;

    @Column(name = "lat_arrivee", nullable = false, precision = 10, scale = 7)
    private BigDecimal latArrivee;

    @Column(name = "lng_arrivee", nullable = false, precision = 10, scale = 7)
    private BigDecimal lngArrivee;

    @Column(name = "date_heure_depart", nullable = false)
    private OffsetDateTime dateHeureDepart;

    @Column(name = "distance_km", precision = 10, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "prix_par_siege", precision = 10, scale = 2)
    private BigDecimal prixParSiege;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTrajet statut = StatutTrajet.PLANIFIE;

    @Column(name = "nb_sieges_reserves", nullable = false)
    private int nbSiegesReserves = 0;
}
