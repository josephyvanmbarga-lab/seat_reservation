package com.mbarga.seat_reservation.suivi;

import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.trajet.Trajet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "position_live")
@Getter @Setter @NoArgsConstructor
public class PositionLive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public PositionLive(User user, Trajet trajet, BigDecimal latitude, BigDecimal longitude) {
        this.user      = user;
        this.trajet    = trajet;
        this.latitude  = latitude;
        this.longitude = longitude;
    }
}
