package com.mbarga.seat_reservation.notification;

import com.mbarga.seat_reservation.reservation.ReservationEvent;

import java.util.List;

public interface NotificationService {

    void                         createFromEvent(ReservationEvent event);
    NotificationResponse         getById(Long id);
    List<NotificationResponse>   getMesNotifications(Long userId);
    NotificationResponse         markAsRead(Long id, Long userId);
    void                         delete(Long id, Long userId);
}
