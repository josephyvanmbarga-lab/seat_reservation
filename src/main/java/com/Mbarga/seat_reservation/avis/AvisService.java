package com.mbarga.seat_reservation.avis;

import com.mbarga.seat_reservation.auth.User;

import java.util.List;

public interface AvisService {

    AvisResponse        create(AvisRequest request, User passager);
    List<AvisResponse>  getByChauffeur(Long chauffeurId);
    List<AvisResponse>  getMesAvis(User chauffeur);
    List<AvisResponse>  getAll();
}
