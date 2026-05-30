package com.mbarga.seat_reservation.reservation;

import com.mbarga.seat_reservation.auth.Role;
import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.trajet.Trajet;
import com.mbarga.seat_reservation.trajet.StatutTrajet;
import com.mbarga.seat_reservation.trajet.TrajetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository  reservationRepository;
    private final TrajetRepository       trajetRepository;
    private final ReservationEventProducer eventProducer;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  TrajetRepository trajetRepository,
                                  ReservationEventProducer eventProducer) {
        this.reservationRepository = reservationRepository;
        this.trajetRepository      = trajetRepository;
        this.eventProducer         = eventProducer;
    }

    @Override
    @Transactional
    public ReservationResponse create(ReservationRequest request, User passager) {
        Trajet trajet = trajetRepository.findById(request.getTrajetId())
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable : id=" + request.getTrajetId()));

        if (trajet.getStatut() != StatutTrajet.PLANIFIE) {
            throw new IllegalStateException("Ce trajet n'accepte plus de réservations");
        }

        if (trajet.getDateHeureDepart().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("La date de départ de ce trajet est dépassée");
        }

        if (request.getSiegeNumero() > trajet.getVehicule().getCapacite()) {
            throw new IllegalArgumentException(
                    "Siège invalide : le véhicule a " + trajet.getVehicule().getCapacite() + " siège(s)");
        }

        if (reservationRepository.existsByTrajetIdAndSiegeNumeroAndStatut(
                trajet.getId(), request.getSiegeNumero(), StatutReservation.CONFIRMEE)) {
            throw new IllegalStateException("Le siège " + request.getSiegeNumero() + " est déjà réservé");
        }

        if (request.getModePaiement() == ModePaiement.MOBILE_MONEY
                && request.getOperateurMobileMoney() == null) {
            throw new IllegalArgumentException("L'opérateur mobile money est obligatoire pour ce mode de paiement");
        }

        Reservation reservation = new Reservation(
                passager,
                trajet,
                request.getSiegeNumero(),
                request.getModePaiement(),
                request.getOperateurMobileMoney(),
                trajet.getPrixParSiege()
        );
        Reservation saved = reservationRepository.save(reservation);

        trajet.setNbSiegesReserves(trajet.getNbSiegesReserves() + 1);
        trajetRepository.save(trajet);

        eventProducer.publish(new ReservationEvent(
                saved.getId(),
                passager.getId(),
                passager.getUsername(),
                saved.getSiegeNumero(),
                trajet.getId(),
                trajet.getPointDepart(),
                trajet.getPointArrivee(),
                trajet.getPrixParSiege()
        ));

        return ReservationResponse.from(saved);
    }

    @Override
    public ReservationResponse getById(Long id, User currentUser) {
        Reservation r = findOrThrow(id);
        if (currentUser.getRole() == Role.ADMIN) return ReservationResponse.from(r);
        if (currentUser.getRole() == Role.CHAUFFEUR &&
                r.getTrajet().getChauffeur().getId().equals(currentUser.getId()))
            return ReservationResponse.from(r);
        if (r.getPassager().getId().equals(currentUser.getId()))
            return ReservationResponse.from(r);
        throw new IllegalStateException("Accès non autorisé à cette réservation");
    }

    @Override
    public List<ReservationResponse> getAll() {
        return reservationRepository.findAll().stream().map(ReservationResponse::from).toList();
    }

    @Override
    public List<ReservationResponse> getMesReservations(User passager) {
        return reservationRepository.findByPassagerId(passager.getId())
                .stream().map(ReservationResponse::from).toList();
    }

    @Override
    public List<ReservationResponse> getByTrajet(Long trajetId) {
        if (!trajetRepository.existsById(trajetId)) {
            throw new IllegalArgumentException("Trajet introuvable : id=" + trajetId);
        }
        return reservationRepository.findByTrajetId(trajetId)
                .stream().map(ReservationResponse::from).toList();
    }

    @Override
    public List<ReservationResponse> getMesTrajetsReservations(User chauffeur) {
        return reservationRepository.findByTrajetChauffeurId(chauffeur.getId())
                .stream().map(ReservationResponse::from).toList();
    }

    @Override
    @Transactional
    public void cancel(Long id, User currentUser) {
        Reservation r = findOrThrow(id);
        if (!r.getPassager().getId().equals(currentUser.getId())
                && currentUser.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Vous ne pouvez annuler que vos propres réservations");
        }
        if (r.getStatut() == StatutReservation.ANNULEE) {
            throw new IllegalStateException("Cette réservation est déjà annulée");
        }
        r.setStatut(StatutReservation.ANNULEE);
        reservationRepository.save(r);

        Trajet trajet = r.getTrajet();
        trajet.setNbSiegesReserves(Math.max(0, trajet.getNbSiegesReserves() - 1));
        trajetRepository.save(trajet);
    }

    @Override
    @Transactional
    public ReservationResponse updateStatut(Long id, StatutReservation statut) {
        Reservation r = findOrThrow(id);
        r.setStatut(statut);
        return ReservationResponse.from(reservationRepository.save(r));
    }

    private Reservation findOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable : id=" + id));
    }
}
