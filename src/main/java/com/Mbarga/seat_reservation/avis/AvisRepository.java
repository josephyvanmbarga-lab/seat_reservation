package com.mbarga.seat_reservation.avis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {

    List<Avis> findByChauffeurId(Long chauffeurId);

    boolean existsByPassagerIdAndTrajetId(Long passagerId, Long trajetId);
}
