package com.mbarga.seat_reservation.notification;

import com.mbarga.seat_reservation.reservation.Reservation;
import com.mbarga.seat_reservation.reservation.ReservationEvent;
import com.mbarga.seat_reservation.reservation.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ReservationRepository reservationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   ReservationRepository reservationRepository) {
        this.notificationRepository = notificationRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public void createFromEvent(ReservationEvent event) {
        Reservation reservation = reservationRepository.findById(event.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Réservation introuvable lors de la création de la notification : id=" + event.getReservationId()));

        String message = String.format(
                "Réservation confirmée pour %s — Siège %d à bord du véhicule %s le %s",
                event.getNomPassager(),
                event.getSiegeNumero(),
                event.getVehiculeImmatriculation(),
                event.getDateVoyage().toLocalDate()
        );

        notificationRepository.save(new Notification(message, reservation));
    }

    @Override
    public NotificationResponse getById(Long id) {
        return NotificationResponse.from(findOrThrow(id));
    }

    @Override
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll().stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    public List<NotificationResponse> getByReservation(Long reservationId) {
        if (!reservationRepository.existsById(reservationId)) {
            throw new IllegalArgumentException("Réservation introuvable : id=" + reservationId);
        }
        return notificationRepository.findByReservationId(reservationId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = findOrThrow(id);
        notification.setLu(true);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Override
    public void delete(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new IllegalArgumentException("Notification introuvable : id=" + id);
        }
        notificationRepository.deleteById(id);
    }

    private Notification findOrThrow(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification introuvable : id=" + id));
    }
}