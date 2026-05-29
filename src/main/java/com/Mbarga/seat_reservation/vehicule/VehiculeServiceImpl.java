package com.mbarga.seat_reservation.vehicule;

import com.mbarga.seat_reservation.reservation.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository repository;
    private final ReservationRepository reservationRepository;

    public VehiculeServiceImpl(VehiculeRepository repository, ReservationRepository reservationRepository) {
        this.repository = repository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public VehiculeResponse create(VehiculeRequest request) {
        if (repository.existsByImmatriculation(request.getImmatriculation())) {
            throw new IllegalStateException("Un véhicule avec cette immatriculation existe déjà");
        }
        Vehicule vehicule = new Vehicule(request.getImmatriculation(), request.getModele(), request.getCapacite());
        return VehiculeResponse.from(repository.save(vehicule));
    }

    @Override
    public VehiculeResponse getById(Long id) {
        Vehicule vehicule = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable : id=" + id));
        return VehiculeResponse.from(vehicule);
    }

    @Override
    public List<VehiculeResponse> getAll() {
        return repository.findAll().stream()
                .map(VehiculeResponse::from)
                .toList();
    }

    @Override
    public VehiculeResponse update(Long id, VehiculeRequest request) {
        Vehicule vehicule = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable : id=" + id));
        if (!vehicule.getImmatriculation().equals(request.getImmatriculation())
                && repository.existsByImmatriculation(request.getImmatriculation())) {
            throw new IllegalStateException("Un véhicule avec cette immatriculation existe déjà");
        }
        vehicule.setImmatriculation(request.getImmatriculation());
        vehicule.setModele(request.getModele());
        vehicule.setCapacite(request.getCapacite());
        return VehiculeResponse.from(repository.save(vehicule));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Véhicule introuvable : id=" + id);
        }
        repository.deleteById(id);
    }

    @Override
    public List<Integer> getSiegesDisponibles(Long vehiculeId, OffsetDateTime date) {
        Vehicule vehicule = repository.findById(vehiculeId)
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable : id=" + vehiculeId));
        List<Integer> reserved = reservationRepository
                .findSiegesReservesByVehiculeIdAndDateVoyage(vehiculeId, date);
        return IntStream.rangeClosed(1, vehicule.getCapacite())
                .filter(s -> !reserved.contains(s))
                .boxed()
                .collect(Collectors.toList());
    }
}