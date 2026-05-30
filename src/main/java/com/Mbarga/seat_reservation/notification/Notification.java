package com.mbarga.seat_reservation.notification;

import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.reservation.Reservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notification")
@Getter @Setter @NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "date_envoi", nullable = false)
    private OffsetDateTime dateEnvoi = OffsetDateTime.now();

    @Column(nullable = false)
    private boolean lu = false;

    public Notification(User user, Reservation reservation, String message) {
        this.user        = user;
        this.reservation = reservation;
        this.message     = message;
    }
}
