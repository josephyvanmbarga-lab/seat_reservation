package com.mbarga.seat_reservation.notification;

import com.mbarga.seat_reservation.reservation.ReservationEvent;

import java.util.List;

public interface NotificationService {

    void createFromEvent(ReservationEvent event);
    NotificationResponse getById(Long id);
    List<NotificationResponse> getAll();
    List<NotificationResponse> getByReservation(Long reservationId);
    NotificationResponse markAsRead(Long id);
    void delete(Long id);
}