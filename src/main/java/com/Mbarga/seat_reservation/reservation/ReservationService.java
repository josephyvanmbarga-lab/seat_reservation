package com.mbarga.seat_reservation.reservation;

import java.util.List;

public interface ReservationService {

    ReservationResponse create(ReservationRequest request);
    ReservationResponse getById(Long id);
    List<ReservationResponse> getAll();
    List<ReservationResponse> getByVehicule(Long vehiculeId);
    void cancel(Long id);
}