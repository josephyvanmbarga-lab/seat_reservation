package com.mbarga.seat_reservation.trajet;

import com.mbarga.seat_reservation.auth.User;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface TrajetService {

    TrajetResponse        create(TrajetRequest request, User chauffeur);
    TrajetResponse        getById(Long id);
    List<TrajetResponse>  search(String pointDepart, String pointArrivee, OffsetDateTime date);
    List<TrajetResponse>  getMesTrajets(User chauffeur);
    List<TrajetResponse>  getHistorique(User user);
    TrajetResponse        demarrer(Long id, User chauffeur);
    TrajetResponse        terminer(Long id, User chauffeur);
    TrajetResponse        annuler(Long id, User chauffeur);
    List<SiegePlanResponse> getPlanSieges(Long id);
    EstimationResponse    estimer(BigDecimal latDepart, BigDecimal lngDepart,
                                  BigDecimal latArrivee, BigDecimal lngArrivee);
}
