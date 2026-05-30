package com.mbarga.seat_reservation.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class NotificationResponse {

    private Long           id;
    private Long           userId;
    private Long           reservationId;
    private String         message;
    private OffsetDateTime dateEnvoi;
    private boolean        lu;

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUser().getId(),
                n.getReservation().getId(),
                n.getMessage(),
                n.getDateEnvoi(),
                n.isLu()
        );
    }
}
