package com.mbarga.seat_reservation.trajet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SiegePlanResponse {
    private int    numero;
    private String statut; // "LIBRE" | "RESERVE"
}
