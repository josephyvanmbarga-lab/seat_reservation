package com.mbarga.seat_reservation.avis;

import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.auth.UserRepository;
import com.mbarga.seat_reservation.reservation.ReservationRepository;
import com.mbarga.seat_reservation.reservation.StatutReservation;
import com.mbarga.seat_reservation.trajet.Trajet;
import com.mbarga.seat_reservation.trajet.StatutTrajet;
import com.mbarga.seat_reservation.trajet.TrajetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AvisServiceImpl implements AvisService {

    private final AvisRepository        avisRepository;
    private final TrajetRepository      trajetRepository;
    private final UserRepository        userRepository;
    private final ReservationRepository reservationRepository;

    public AvisServiceImpl(AvisRepository avisRepository,
                           TrajetRepository trajetRepository,
                           UserRepository userRepository,
                           ReservationRepository reservationRepository) {
        this.avisRepository        = avisRepository;
        this.trajetRepository      = trajetRepository;
        this.userRepository        = userRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public AvisResponse create(AvisRequest request, User passager) {
        Trajet trajet = trajetRepository.findById(request.getTrajetId())
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable : id=" + request.getTrajetId()));

        if (trajet.getStatut() != StatutTrajet.TERMINE) {
            throw new IllegalStateException("Vous ne pouvez noter qu'un trajet terminé");
        }

        boolean aReserve = reservationRepository.findByPassagerId(passager.getId())
                .stream()
                .anyMatch(r -> r.getTrajet().getId().equals(trajet.getId())
                        && r.getStatut() == StatutReservation.CONFIRMEE);
        if (!aReserve) {
            throw new IllegalStateException("Vous devez avoir effectué ce trajet pour le noter");
        }

        if (avisRepository.existsByPassagerIdAndTrajetId(passager.getId(), trajet.getId())) {
            throw new IllegalStateException("Vous avez déjà noté ce trajet");
        }

        User chauffeur = trajet.getChauffeur();
        Avis avis = avisRepository.save(
                new Avis(passager, chauffeur, trajet, request.getNote(), request.getCommentaire()));

        updateNoteMoyenne(chauffeur);

        return AvisResponse.from(avis);
    }

    @Override
    public List<AvisResponse> getByChauffeur(Long chauffeurId) {
        return avisRepository.findByChauffeurId(chauffeurId)
                .stream().map(AvisResponse::from).toList();
    }

    @Override
    public List<AvisResponse> getAll() {
        return avisRepository.findAll().stream().map(AvisResponse::from).toList();
    }

    private void updateNoteMoyenne(User chauffeur) {
        List<Avis> tousAvis = avisRepository.findByChauffeurId(chauffeur.getId());
        double moyenne = tousAvis.stream()
                .mapToInt(Avis::getNote)
                .average()
                .orElse(0.0);
        chauffeur.setNoteMoyenne(Math.round(moyenne * 100.0) / 100.0);
        chauffeur.setNbAvis(tousAvis.size());
        userRepository.save(chauffeur);
    }
}
