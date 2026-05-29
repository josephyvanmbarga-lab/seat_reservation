package com.mbarga.seat_reservation.reservation;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventProducer {

    static final String TOPIC = "reservation-created";

    private final KafkaTemplate<String, ReservationEvent> kafkaTemplate;

    public ReservationEventProducer(KafkaTemplate<String, ReservationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ReservationEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.getReservationId()), event);
    }
}