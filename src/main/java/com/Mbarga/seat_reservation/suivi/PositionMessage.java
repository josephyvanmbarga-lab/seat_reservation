package com.mbarga.seat_reservation.suivi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionMessage {
    private Long           userId;
    private String         username;
    private String         role;
    private Long           trajetId;
    private BigDecimal     latitude;
    private BigDecimal     longitude;
    private OffsetDateTime timestamp;
}
