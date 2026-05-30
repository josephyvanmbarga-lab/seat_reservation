package com.mbarga.seat_reservation.reservation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEvent {
    private Long       reservationId;
    private Long       passagerId;
    private String     passagerUsername;
    private int        siegeNumero;
    private Long       trajetId;
    private String     pointDepart;
    private String     pointArrivee;
    private BigDecimal prixPaye;
}
