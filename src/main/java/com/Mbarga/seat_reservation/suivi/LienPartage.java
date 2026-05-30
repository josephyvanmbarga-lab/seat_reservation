package com.mbarga.seat_reservation.suivi;

import com.mbarga.seat_reservation.reservation.Reservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "lien_partage")
@Getter @Setter @NoArgsConstructor
public class LienPartage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "expire_at", nullable = false)
    private OffsetDateTime expireAt;

    @Column(nullable = false)
    private boolean actif = true;

    public LienPartage(String token, Reservation reservation, OffsetDateTime expireAt) {
        this.token       = token;
        this.reservation = reservation;
        this.expireAt    = expireAt;
    }
}
