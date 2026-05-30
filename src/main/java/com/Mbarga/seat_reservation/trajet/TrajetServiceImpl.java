package com.mbarga.seat_reservation.trajet;

import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.tarification.HereMapsService;
import com.mbarga.seat_reservation.vehicule.Vehicule;
import com.mbarga.seat_reservation.vehicule.VehiculeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository    trajetRepository;
    private final VehiculeRepository  vehiculeRepository;
    private final HereMapsService     hereMapsService;

    public TrajetServiceImpl(TrajetRepository trajetRepository,
                             VehiculeRepository vehiculeRepository,
                             HereMapsService hereMapsService) {
        this.trajetRepository   = trajetRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.hereMapsService    = hereMapsService;
    }

    @Override
    @Transactional
    public TrajetResponse create(TrajetRequest request, User chauffeur) {
        Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable : id=" + request.getVehiculeId()));

        if (!vehicule.getChauffeur().getId().equals(chauffeur.getId())) {
            throw new IllegalStateException("Ce véhicule ne vous appartient pas");
        }

        EstimationResponse estimation = hereMapsService.estimer(
                request.getLatDepart(), request.getLngDepart(),
                request.getLatArrivee(), request.getLngArrivee());

        Trajet trajet = new Trajet();
        trajet.setChauffeur(chauffeur);
        trajet.setVehicule(vehicule);
        trajet.setPointDepart(request.getPointDepart());
        trajet.setLatDepart(request.getLatDepart());
        trajet.setLngDepart(request.getLngDepart());
        trajet.setPointArrivee(request.getPointArrivee());
        trajet.setLatArrivee(request.getLatArrivee());
        trajet.setLngArrivee(request.getLngArrivee());
        trajet.setDateHeureDepart(request.getDateHeureDepart());
        trajet.setDistanceKm(estimation.getDistanceKm());
        trajet.setPrixParSiege(estimation.getPrixEstime());

        return TrajetResponse.from(trajetRepository.save(trajet));
    }

    @Override
    public TrajetResponse getById(Long id) {
        return TrajetResponse.from(findOrThrow(id));
    }

    @Override
    public List<TrajetResponse> search(String pointDepart, String pointArrivee, OffsetDateTime date) {
        return trajetRepository.search(pointDepart, pointArrivee, date)
                .stream().map(TrajetResponse::from).toList();
    }

    @Override
    public List<TrajetResponse> getMesTrajets(User chauffeur) {
        return trajetRepository.findByChauffeurId(chauffeur.getId())
                .stream().map(TrajetResponse::from).toList();
    }

    @Override
    public List<TrajetResponse> getHistorique(User user) {
        return trajetRepository.findByChauffeurId(user.getId())
                .stream()
                .filter(t -> t.getStatut() == StatutTrajet.TERMINE)
                .map(TrajetResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public TrajetResponse demarrer(Long id, User chauffeur) {
        Trajet trajet = findOrThrow(id);
        verifierPropriete(trajet, chauffeur);
        if (trajet.getStatut() != StatutTrajet.PLANIFIE) {
            throw new IllegalStateException("Seul un trajet PLANIFIE peut être démarré");
        }
        trajet.setStatut(StatutTrajet.EN_COURS);
        return TrajetResponse.from(trajetRepository.save(trajet));
    }

    @Override
    @Transactional
    public TrajetResponse terminer(Long id, User chauffeur) {
        Trajet trajet = findOrThrow(id);
        verifierPropriete(trajet, chauffeur);
        if (trajet.getStatut() != StatutTrajet.EN_COURS) {
            throw new IllegalStateException("Seul un trajet EN_COURS peut être terminé");
        }
        trajet.setStatut(StatutTrajet.TERMINE);
        return TrajetResponse.from(trajetRepository.save(trajet));
    }

    @Override
    @Transactional
    public TrajetResponse annuler(Long id, User chauffeur) {
        Trajet trajet = findOrThrow(id);
        verifierPropriete(trajet, chauffeur);
        if (trajet.getStatut() == StatutTrajet.TERMINE) {
            throw new IllegalStateException("Impossible d'annuler un trajet déjà terminé");
        }
        trajet.setStatut(StatutTrajet.ANNULE);
        return TrajetResponse.from(trajetRepository.save(trajet));
    }

    @Override
    public List<SiegePlanResponse> getPlanSieges(Long id) {
        Trajet trajet = findOrThrow(id);
        List<Integer> reserves = trajetRepository.findSiegesReservesByTrajetId(id);
        return IntStream.rangeClosed(1, trajet.getVehicule().getCapacite())
                .mapToObj(n -> new SiegePlanResponse(n, reserves.contains(n) ? "RESERVE" : "LIBRE"))
                .collect(Collectors.toList());
    }

    @Override
    public EstimationResponse estimer(BigDecimal latDepart, BigDecimal lngDepart,
                                       BigDecimal latArrivee, BigDecimal lngArrivee) {
        return hereMapsService.estimer(latDepart, lngDepart, latArrivee, lngArrivee);
    }

    private Trajet findOrThrow(Long id) {
        return trajetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable : id=" + id));
    }

    private void verifierPropriete(Trajet trajet, User chauffeur) {
        if (!trajet.getChauffeur().getId().equals(chauffeur.getId())) {
            throw new IllegalStateException("Ce trajet ne vous appartient pas");
        }
    }
}
