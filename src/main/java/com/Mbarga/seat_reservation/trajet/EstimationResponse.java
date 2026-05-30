package com.mbarga.seat_reservation.trajet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class EstimationResponse {
    private BigDecimal distanceKm;
    private long       dureeSec;
    private BigDecimal prixEstime;
}
