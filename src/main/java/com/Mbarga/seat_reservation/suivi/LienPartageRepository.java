package com.mbarga.seat_reservation.suivi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LienPartageRepository extends JpaRepository<LienPartage, Long> {

    Optional<LienPartage> findByTokenAndActifTrue(String token);

    Optional<LienPartage> findByReservationIdAndActifTrue(Long reservationId);
}
