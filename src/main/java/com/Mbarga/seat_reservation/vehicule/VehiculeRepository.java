package com.mbarga.seat_reservation.vehicule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    boolean existsByImmatriculation(String immatriculation);

    boolean existsByImmatriculationAndIdNot(String immatriculation, Long id);

    List<Vehicule> findByChauffeurId(Long chauffeurId);
}
