package com.mbarga.seat_reservation.notification;

import com.mbarga.seat_reservation.reservation.ReservationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "reservation-created",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ReservationEvent event) {
        notificationService.createFromEvent(event);
    }
}