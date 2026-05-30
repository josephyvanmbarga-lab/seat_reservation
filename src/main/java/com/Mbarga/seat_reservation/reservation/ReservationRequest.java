package com.mbarga.seat_reservation.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationRequest {

    @NotNull(message = "L'identifiant du trajet est obligatoire")
    private Long trajetId;

    @Min(value = 1, message = "Le numéro de siège doit être au moins 1")
    private int siegeNumero;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private ModePaiement modePaiement;

    private OperateurMobileMoney operateurMobileMoney;
}
