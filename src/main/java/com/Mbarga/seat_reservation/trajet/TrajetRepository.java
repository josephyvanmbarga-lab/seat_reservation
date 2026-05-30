package com.mbarga.seat_reservation.trajet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    List<Trajet> findByChauffeurId(Long chauffeurId);

    List<Trajet> findByStatut(StatutTrajet statut);

    @Query("""
        SELECT t FROM Trajet t
        WHERE t.statut = 'PLANIFIE'
          AND (:pointDepart IS NULL OR LOWER(t.pointDepart) LIKE LOWER(CONCAT('%', :pointDepart, '%')))
          AND (:pointArrivee IS NULL OR LOWER(t.pointArrivee) LIKE LOWER(CONCAT('%', :pointArrivee, '%')))
          AND (:date IS NULL OR CAST(t.dateHeureDepart AS date) = CAST(:date AS date))
        """)
    List<Trajet> search(@Param("pointDepart") String pointDepart,
                        @Param("pointArrivee") String pointArrivee,
                        @Param("date") OffsetDateTime date);

    @Query("SELECT r.siegeNumero FROM Reservation r WHERE r.trajet.id = :trajetId AND r.statut = 'CONFIRMEE'")
    List<Integer> findSiegesReservesByTrajetId(@Param("trajetId") Long trajetId);
}
