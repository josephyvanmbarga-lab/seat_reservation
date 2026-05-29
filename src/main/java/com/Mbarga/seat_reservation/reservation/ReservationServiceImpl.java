package com.mbarga.seat_reservation.reservation;

import com.mbarga.seat_reservation.vehicule.Vehicule;
import com.mbarga.seat_reservation.vehicule.VehiculeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ReservationEventProducer eventProducer;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  VehiculeRepository vehiculeRepository,
                                  ReservationEventProducer eventProducer) {
        this.reservationRepository = reservationRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.eventProducer = eventProducer;
    }

    @Override
    public ReservationResponse create(ReservationRequest request) {
        if (request.getNomPassager() == null || request.getNomPassager().isBlank()) {
            throw new IllegalArgumentException("Le nom du passager est obligatoire");
        }
        if (request.getDateVoyage() == null) {
            throw new IllegalArgumentException("La date de voyage est obligatoire");
        }

        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable : id=" + request.getVehiculeId()));

        if (request.getSiegeNumero() < 1 || request.getSiegeNumero() > vehicule.getCapacite()) {
            throw new IllegalArgumentException(
                    "Siège invalide : le véhicule a " + vehicule.getCapacite() + " siège(s)");
        }

        if (reservationRepository.existsByVehiculeIdAndDateVoyageAndSiegeNumero(
                vehicule.getId(), request.getDateVoyage(), request.getSiegeNumero())) {
            throw new IllegalStateException(
                    "Le siège " + request.getSiegeNumero() + " est déjà réservé pour ce voyage");
        }

        Reservation reservation = new Reservation(
                request.getNomPassager(),
                request.getSiegeNumero(),
                request.getDateVoyage(),
                vehicule
        );
        Reservation saved = reservationRepository.save(reservation);

        eventProducer.publish(new ReservationEvent(
                saved.getId(),
                saved.getNomPassager(),
                saved.getSiegeNumero(),
                vehicule.getId(),
                vehicule.getImmatriculation(),
                saved.getDateVoyage()
        ));

        return ReservationResponse.from(saved);
    }

    @Override
    public ReservationResponse getById(Long id) {
        return ReservationResponse.from(findOrThrow(id));
    }

    @Override
    public List<ReservationResponse> getAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Override
    public List<ReservationResponse> getByVehicule(Long vehiculeId) {
        if (!vehiculeRepository.existsById(vehiculeId)) {
            throw new IllegalArgumentException("Véhicule introuvable : id=" + vehiculeId);
        }
        return reservationRepository.findByVehiculeId(vehiculeId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Override
    public void cancel(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new IllegalArgumentException("Réservation introuvable : id=" + id);
        }
        reservationRepository.deleteById(id);
    }

    private Reservation findOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable : id=" + id));
    }
}