package com.mbarga.seat_reservation.suivi;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionLiveRepository extends JpaRepository<PositionLive, Long> {

    Optional<PositionLive> findByUserIdAndTrajetId(Long userId, Long trajetId);

    List<PositionLive> findByTrajetId(Long trajetId);
}
