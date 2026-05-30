package com.mbarga.seat_reservation.reservation;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class ReservationResponse {

    private Long                 id;
    private Long                 passagerId;
    private String               passagerUsername;
    private Long                 trajetId;
    private String               pointDepart;
    private String               pointArrivee;
    private OffsetDateTime       dateHeureDepart;
    private int                  siegeNumero;
    private StatutReservation    statut;
    private ModePaiement         modePaiement;
    private OperateurMobileMoney operateurMobileMoney;
    private BigDecimal           prixPaye;
    private OffsetDateTime       dateReservation;

    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getPassager().getId(),
                r.getPassager().getUsername(),
                r.getTrajet().getId(),
                r.getTrajet().getPointDepart(),
                r.getTrajet().getPointArrivee(),
                r.getTrajet().getDateHeureDepart(),
                r.getSiegeNumero(),
                r.getStatut(),
                r.getModePaiement(),
                r.getOperateurMobileMoney(),
                r.getPrixPaye(),
                r.getDateReservation()
        );
    }
}
