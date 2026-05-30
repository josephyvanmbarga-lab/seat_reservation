package com.mbarga.seat_reservation.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTrajetIdAndSiegeNumeroAndStatut(Long trajetId, int siegeNumero, StatutReservation statut);

    List<Reservation> findByPassagerId(Long passagerId);

    List<Reservation> findByTrajetId(Long trajetId);

    List<Reservation> findByTrajetChauffeurId(Long chauffeurId);
}
