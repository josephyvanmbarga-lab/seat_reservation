package com.mbarga.seat_reservation.reservation;

import com.mbarga.seat_reservation.auth.User;

import java.util.List;

public interface ReservationService {

    ReservationResponse        create(ReservationRequest request, User passager);
    ReservationResponse        getById(Long id, User currentUser);
    List<ReservationResponse>  getAll();
    List<ReservationResponse>  getMesReservations(User passager);
    List<ReservationResponse>  getByTrajet(Long trajetId);
    List<ReservationResponse>  getMesTrajetsReservations(User chauffeur);
    void                       cancel(Long id, User currentUser);
    ReservationResponse        updateStatut(Long id, StatutReservation statut);
}
