package com.Mbarga.seat_reservation.notification;

import com.mbarga.seat_reservation.auth.Role;
import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.auth.UserRepository;
import com.mbarga.seat_reservation.notification.*;
import com.mbarga.seat_reservation.reservation.Reservation;
import com.mbarga.seat_reservation.reservation.ReservationEvent;
import com.mbarga.seat_reservation.reservation.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock NotificationRepository notificationRepository;
    @Mock ReservationRepository  reservationRepository;
    @Mock UserRepository         userRepository;
    @InjectMocks NotificationServiceImpl service;

    // ── helpers ───────────────────────────────────────────────────────────────

    private User user(Long id) {
        User u = new User("user" + id, "pass", "u" + id + "@test.com", "+237600000000", Role.USER);
        u.setId(id);
        return u;
    }

    private Reservation reservation(Long id) {
        Reservation r = new Reservation();
        r.setId(id);
        return r;
    }

    private Notification notification(Long id, User user, Reservation r) {
        Notification n = new Notification(user, r, "Message de test");
        n.setId(id);
        return n;
    }

    private ReservationEvent event(Long reservationId, Long passagerId) {
        return new ReservationEvent(reservationId, passagerId, "Alice", 3,
                1L, "Yaoundé", "Douala", BigDecimal.valueOf(3500));
    }

    // ── createFromEvent ───────────────────────────────────────────────────────

    @Test
    void createFromEvent_success() {
        User passager = user(1L);
        Reservation r = reservation(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createFromEvent(event(1L, 1L));

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createFromEvent_messageContientSiegeEtTrajet() {
        User passager = user(1L);
        Reservation r = reservation(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(userRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.createFromEvent(event(1L, 1L));

        verify(notificationRepository).save(argThat(n ->
                n.getMessage().contains("3") && n.getMessage().contains("Yaoundé")));
    }

    @Test
    void createFromEvent_reservationIntrouvable_throws() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createFromEvent(event(99L, 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createFromEvent_userIntrouvable_throws() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation(1L)));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createFromEvent(event(1L, 99L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_introuvable_throws() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // ── getMesNotifications ───────────────────────────────────────────────────

    @Test
    void getMesNotifications_retourneListe() {
        User u = user(1L);
        Reservation r = reservation(1L);
        when(notificationRepository.findByUserId(1L))
                .thenReturn(List.of(
                        notification(1L, u, r),
                        notification(2L, u, r)
                ));

        assertThat(service.getMesNotifications(1L)).hasSize(2);
    }

    // ── markAsRead ────────────────────────────────────────────────────────────

    @Test
    void markAsRead_success() {
        User u = user(1L);
        Notification n = notification(1L, u, reservation(1L));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any())).thenReturn(n);

        NotificationResponse resp = service.markAsRead(1L, 1L);

        assertThat(resp.isLu()).isTrue();
        assertThat(n.isLu()).isTrue();
    }

    @Test
    void markAsRead_nonProprietaire_throws() {
        User u = user(1L);
        Notification n = notification(1L, u, reservation(1L));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.markAsRead(1L, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("appartient");
    }

    @Test
    void markAsRead_introuvable_throws() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_success() {
        User u = user(1L);
        Notification n = notification(1L, u, reservation(1L));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        service.delete(1L, 1L);

        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void delete_nonProprietaire_throws() {
        User u = user(1L);
        Notification n = notification(1L, u, reservation(1L));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.delete(1L, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("appartient");
    }

    @Test
    void delete_introuvable_throws() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}