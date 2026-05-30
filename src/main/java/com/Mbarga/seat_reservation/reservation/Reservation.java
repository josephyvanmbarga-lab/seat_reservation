package com.mbarga.seat_reservation.reservation;

import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.trajet.Trajet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reservation")
@Getter @Setter @NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passager_id", nullable = false)
    private User passager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

    @Column(name = "siege_numero", nullable = false)
    private int siegeNumero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutReservation statut = StatutReservation.CONFIRMEE;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false, length = 20)
    private ModePaiement modePaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "operateur_mobile_money", length = 20)
    private OperateurMobileMoney operateurMobileMoney;

    @Column(name = "prix_paye", precision = 10, scale = 2)
    private BigDecimal prixPaye;

    @Column(name = "date_reservation", nullable = false)
    private OffsetDateTime dateReservation = OffsetDateTime.now();

    public Reservation(User passager, Trajet trajet, int siegeNumero,
                       ModePaiement modePaiement, OperateurMobileMoney operateurMobileMoney,
                       BigDecimal prixPaye) {
        this.passager               = passager;
        this.trajet                 = trajet;
        this.siegeNumero            = siegeNumero;
        this.modePaiement           = modePaiement;
        this.operateurMobileMoney   = operateurMobileMoney;
        this.prixPaye               = prixPaye;
    }
}
