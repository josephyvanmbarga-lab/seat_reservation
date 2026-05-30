package com.mbarga.seat_reservation.notification;

import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.auth.UserRepository;
import com.mbarga.seat_reservation.reservation.Reservation;
import com.mbarga.seat_reservation.reservation.ReservationEvent;
import com.mbarga.seat_reservation.reservation.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ReservationRepository  reservationRepository;
    private final UserRepository         userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   ReservationRepository reservationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.reservationRepository  = reservationRepository;
        this.userRepository         = userRepository;
    }

    @Override
    @Transactional
    public void createFromEvent(ReservationEvent event) {
        Reservation reservation = reservationRepository.findById(event.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Réservation introuvable : id=" + event.getReservationId()));

        User passager = userRepository.findById(event.getPassagerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilisateur introuvable : id=" + event.getPassagerId()));

        String message = String.format(
                "Réservation confirmée — Siège %d | %s → %s | Prix : %.0f FCFA",
                event.getSiegeNumero(),
                event.getPointDepart(),
                event.getPointArrivee(),
                event.getPrixPaye()
        );

        notificationRepository.save(new Notification(passager, reservation, message));
    }

    @Override
    public NotificationResponse getById(Long id) {
        return NotificationResponse.from(findOrThrow(id));
    }

    @Override
    public List<NotificationResponse> getMesNotifications(Long userId) {
        return notificationRepository.findByUserId(userId)
                .stream().map(NotificationResponse::from).toList();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id, Long userId) {
        Notification n = findOrThrow(id);
        if (!n.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Cette notification ne vous appartient pas");
        }
        n.setLu(true);
        return NotificationResponse.from(notificationRepository.save(n));
    }

    @Override
    public void delete(Long id, Long userId) {
        Notification n = findOrThrow(id);
        if (!n.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Cette notification ne vous appartient pas");
        }
        notificationRepository.deleteById(id);
    }

    private Notification findOrThrow(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification introuvable : id=" + id));
    }
}
