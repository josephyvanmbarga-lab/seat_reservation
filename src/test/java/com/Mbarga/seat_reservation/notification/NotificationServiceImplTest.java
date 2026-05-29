package com.Mbarga.seat_reservation.notification;

import com.mbarga.seat_reservation.notification.Notification;
import com.mbarga.seat_reservation.notification.NotificationRepository;
import com.mbarga.seat_reservation.notification.NotificationServiceImpl;
import com.mbarga.seat_reservation.reservation.Reservation;
import com.mbarga.seat_reservation.reservation.ReservationEvent;
import com.mbarga.seat_reservation.reservation.ReservationRepository;
import com.mbarga.seat_reservation.vehicule.Vehicule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock NotificationRepository notificationRepository;
    @Mock ReservationRepository reservationRepository;
    @InjectMocks NotificationServiceImpl service;

    private Reservation reservation() {
        Vehicule v = new Vehicule("AB-123-CD", "Bus", 10);
        return new Reservation("Alice", 3, OffsetDateTime.now().plusDays(1), v);
    }

    private ReservationEvent event(Long reservationId) {
        return new ReservationEvent(reservationId, "Alice", 3, 1L, "AB-123-CD",
                OffsetDateTime.now().plusDays(1));
    }

    @Test
    void createFromEvent_success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation()));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.createFromEvent(event(1L));

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createFromEvent_messageContientPassagerEtSiege() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation()));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.createFromEvent(event(1L));

        verify(notificationRepository).save(argThat(n ->
                n.getMessage().contains("Alice") && n.getMessage().contains("3")
        ));
    }

    @Test
    void createFromEvent_reservationIntrouvable_throws() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createFromEvent(event(99L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void markAsRead_success() {
        Notification n = new Notification("message", reservation());
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any())).thenReturn(n);

        service.markAsRead(1L);

        assertThat(n.isLu()).isTrue();
    }

    @Test
    void markAsRead_introuvable_throws() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_success() {
        when(notificationRepository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void delete_introuvable_throws() {
        when(notificationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_retourneTout() {
        Reservation r = reservation();
        when(notificationRepository.findAll())
                .thenReturn(List.of(
                        new Notification("msg1", r),
                        new Notification("msg2", r)
                ));

        assertThat(service.getAll()).hasSize(2);
    }

    @Test
    void getByReservation_reservationIntrouvable_throws() {
        when(reservationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getByReservation(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}