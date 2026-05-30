package com.mbarga.seat_reservation.suivi;

import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.auth.UserRepository;
import com.mbarga.seat_reservation.reservation.Reservation;
import com.mbarga.seat_reservation.reservation.ReservationRepository;
import com.mbarga.seat_reservation.trajet.Trajet;
import com.mbarga.seat_reservation.trajet.TrajetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SuiviServiceImpl implements SuiviService {

    private final PositionLiveRepository  positionLiveRepository;
    private final LienPartageRepository   lienPartageRepository;
    private final UserRepository          userRepository;
    private final TrajetRepository        trajetRepository;
    private final ReservationRepository   reservationRepository;

    public SuiviServiceImpl(PositionLiveRepository positionLiveRepository,
                             LienPartageRepository lienPartageRepository,
                             UserRepository userRepository,
                             TrajetRepository trajetRepository,
                             ReservationRepository reservationRepository) {
        this.positionLiveRepository = positionLiveRepository;
        this.lienPartageRepository  = lienPartageRepository;
        this.userRepository         = userRepository;
        this.trajetRepository       = trajetRepository;
        this.reservationRepository  = reservationRepository;
    }

    @Override
    @Transactional
    public void updatePosition(Long userId, Long trajetId,
                               BigDecimal latitude, BigDecimal longitude) {
        User user     = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable"));

        positionLiveRepository.findByUserIdAndTrajetId(userId, trajetId)
                .ifPresentOrElse(p -> {
                    p.setLatitude(latitude);
                    p.setLongitude(longitude);
                    p.setUpdatedAt(OffsetDateTime.now());
                    positionLiveRepository.save(p);
                }, () -> positionLiveRepository.save(
                        new PositionLive(user, trajet, latitude, longitude)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionMessage> getPositionsByTrajet(Long trajetId) {
        return positionLiveRepository.findByTrajetId(trajetId)
                .stream()
                .map(p -> new PositionMessage(
                        p.getUser().getId(),
                        p.getUser().getUsername(),
                        p.getUser().getRole().name(),
                        p.getTrajet().getId(),
                        p.getLatitude(),
                        p.getLongitude(),
                        p.getUpdatedAt()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionMessage> getPositionsByToken(String token) {
        LienPartage lien = lienPartageRepository.findByTokenAndActifTrue(token)
                .filter(l -> l.getExpireAt().isAfter(OffsetDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException("Lien invalide ou expiré"));
        return getPositionsByTrajet(lien.getReservation().getTrajet().getId());
    }

    @Override
    @Transactional
    public LienPartageResponse genererLien(Long reservationId, User passager) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable : id=" + reservationId));

        if (!reservation.getPassager().getId().equals(passager.getId())) {
            throw new IllegalStateException("Cette réservation ne vous appartient pas");
        }

        lienPartageRepository.findByReservationIdAndActifTrue(reservationId)
                .ifPresent(l -> { l.setActif(false); lienPartageRepository.save(l); });

        String token = UUID.randomUUID().toString();
        OffsetDateTime expireAt = reservation.getTrajet().getDateHeureDepart().plusHours(12);
        LienPartage lien = lienPartageRepository.save(new LienPartage(token, reservation, expireAt));

        return new LienPartageResponse(token, reservationId, expireAt,
                "/api/suivi/" + token);
    }

    @Override
    @Transactional
    public void desactiverLien(String token, User passager) {
        LienPartage lien = lienPartageRepository.findByTokenAndActifTrue(token)
                .orElseThrow(() -> new IllegalArgumentException("Lien introuvable ou déjà désactivé"));

        if (!lien.getReservation().getPassager().getId().equals(passager.getId())) {
            throw new IllegalStateException("Ce lien ne vous appartient pas");
        }
        lien.setActif(false);
        lienPartageRepository.save(lien);
    }

    @Override
    public LienPartage getLienActif(String token) {
        return lienPartageRepository.findByTokenAndActifTrue(token)
                .filter(l -> l.getExpireAt().isAfter(OffsetDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException("Lien invalide ou expiré"));
    }
}
