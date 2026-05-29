package com.mbarga.seat_reservation.notification;

import java.time.OffsetDateTime;

public class NotificationResponse {

    private Long id;
    private String message;
    private OffsetDateTime dateEnvoi;
    private boolean lu;
    private Long reservationId;

    public NotificationResponse() {}

    public NotificationResponse(Long id, String message, OffsetDateTime dateEnvoi, boolean lu, Long reservationId) {
        this.id = id;
        this.message = message;
        this.dateEnvoi = dateEnvoi;
        this.lu = lu;
        this.reservationId = reservationId;
    }

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getMessage(),
                n.getDateEnvoi(),
                n.isLu(),
                n.getReservation().getId()
        );
    }

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public OffsetDateTime getDateEnvoi() { return dateEnvoi; }
    public boolean isLu() { return lu; }
    public Long getReservationId() { return reservationId; }
}