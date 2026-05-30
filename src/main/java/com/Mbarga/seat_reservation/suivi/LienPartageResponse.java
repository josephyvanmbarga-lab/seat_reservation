package com.mbarga.seat_reservation.suivi;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class LienPartageResponse {
    private String         token;
    private Long           reservationId;
    private OffsetDateTime expireAt;
    private String         lienSuivi;
}
