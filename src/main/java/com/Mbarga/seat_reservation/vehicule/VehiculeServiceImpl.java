package com.mbarga.seat_reservation.vehicule;

import com.mbarga.seat_reservation.auth.Role;
import com.mbarga.seat_reservation.auth.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository repository;

    public VehiculeServiceImpl(VehiculeRepository repository) {
        this.repository = repository;
    }

    @Override
    public VehiculeResponse create(VehiculeRequest request, User chauffeur) {
        if (repository.existsByImmatriculation(request.getImmatriculation())) {
            throw new IllegalStateException("Un véhicule avec cette immatriculation existe déjà");
        }
        Vehicule vehicule = new Vehicule(
                request.getImmatriculation(),
                request.getModele(),
                request.getCapacite(),
                request.getTypeDisposition(),
                chauffeur
        );
        return VehiculeResponse.from(repository.save(vehicule));
    }

    @Override
    public VehiculeResponse getById(Long id) {
        return VehiculeResponse.from(findOrThrow(id));
    }

    @Override
    public List<VehiculeResponse> getAll() {
        return repository.findAll().stream().map(VehiculeResponse::from).toList();
    }

    @Override
    public List<VehiculeResponse> getMesVehicules(User chauffeur) {
        return repository.findByChauffeurId(chauffeur.getId())
                .stream().map(VehiculeResponse::from).toList();
    }

    @Override
    public VehiculeResponse update(Long id, VehiculeRequest request, User currentUser) {
        Vehicule vehicule = findOrThrow(id);

        if (currentUser.getRole() != Role.ADMIN
                && !vehicule.getChauffeur().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Ce véhicule ne vous appartient pas");
        }
        if (!vehicule.getImmatriculation().equals(request.getImmatriculation())
                && repository.existsByImmatriculationAndIdNot(request.getImmatriculation(), id)) {
            throw new IllegalStateException("Un véhicule avec cette immatriculation existe déjà");
        }
        vehicule.setImmatriculation(request.getImmatriculation());
        vehicule.setModele(request.getModele());
        vehicule.setCapacite(request.getCapacite());
        vehicule.setTypeDisposition(request.getTypeDisposition());
        return VehiculeResponse.from(repository.save(vehicule));
    }

    @Override
    public void delete(Long id, User currentUser) {
        Vehicule vehicule = findOrThrow(id);
        if (currentUser.getRole() != Role.ADMIN
                && !vehicule.getChauffeur().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Ce véhicule ne vous appartient pas");
        }
        repository.deleteById(id);
    }

    private Vehicule findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable : id=" + id));
    }
}
