package com.mbarga.seat_reservation.suivi;

import com.mbarga.seat_reservation.auth.User;

import java.math.BigDecimal;
import java.util.List;

public interface SuiviService {

    void                    updatePosition(Long userId, Long trajetId,
                                           BigDecimal latitude, BigDecimal longitude);
    List<PositionMessage>   getPositionsByTrajet(Long trajetId);
    List<PositionMessage>   getPositionsByToken(String token);
    LienPartageResponse     genererLien(Long reservationId, User passager);
    void                    desactiverLien(String token, User passager);
    LienPartage             getLienActif(String token);
}
