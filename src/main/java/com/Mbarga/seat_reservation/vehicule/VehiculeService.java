package com.mbarga.seat_reservation.vehicule;

import java.time.OffsetDateTime;
import java.util.List;

public interface VehiculeService {

    VehiculeResponse create(VehiculeRequest request);
    VehiculeResponse getById(Long id);
    List<VehiculeResponse> getAll();
    VehiculeResponse update(Long id, VehiculeRequest request);
    void delete(Long id);
    List<Integer> getSiegesDisponibles(Long vehiculeId, OffsetDateTime date);
}