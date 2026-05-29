package com.mbarga.seat_reservation.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByVehiculeIdAndDateVoyageAndSiegeNumero(Long vehiculeId, OffsetDateTime dateVoyage, int siegeNumero);

    List<Reservation> findByVehiculeId(Long vehiculeId);

    @Query("SELECT r.siegeNumero FROM Reservation r WHERE r.vehicule.id = :vehiculeId AND r.dateVoyage = :dateVoyage")
    List<Integer> findSiegesReservesByVehiculeIdAndDateVoyage(@Param("vehiculeId") Long vehiculeId,
                                                              @Param("dateVoyage") OffsetDateTime dateVoyage);
}