package com.mbarga.seat_reservation.vehicule;

import com.mbarga.seat_reservation.auth.User;

import java.util.List;

public interface VehiculeService {

    VehiculeResponse        create(VehiculeRequest request, User chauffeur);
    VehiculeResponse        getById(Long id);
    List<VehiculeResponse>  getAll();
    List<VehiculeResponse>  getMesVehicules(User chauffeur);
    VehiculeResponse        update(Long id, VehiculeRequest request, User currentUser);
    void                    delete(Long id, User currentUser);
}
