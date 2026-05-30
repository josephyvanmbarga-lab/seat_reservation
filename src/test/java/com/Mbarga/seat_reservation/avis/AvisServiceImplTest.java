package com.Mbarga.seat_reservation.avis;

import com.mbarga.seat_reservation.auth.Role;
import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.auth.UserRepository;
import com.mbarga.seat_reservation.avis.*;
import com.mbarga.seat_reservation.reservation.Reservation;
import com.mbarga.seat_reservation.reservation.ReservationRepository;
import com.mbarga.seat_reservation.reservation.StatutReservation;
import com.mbarga.seat_reservation.trajet.StatutTrajet;
import com.mbarga.seat_reservation.trajet.Trajet;
import com.mbarga.seat_reservation.trajet.TrajetRepository;
import com.mbarga.seat_reservation.vehicule.TypeDisposition;
import com.mbarga.seat_reservation.vehicule.Vehicule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvisServiceImplTest {

    @Mock AvisRepository        avisRepository;
    @Mock TrajetRepository      trajetRepository;
    @Mock UserRepository        userRepository;
    @Mock ReservationRepository reservationRepository;
    @InjectMocks AvisServiceImpl service;

    // ── helpers ───────────────────────────────────────────────────────────────

    private User user(Long id, Role role) {
        User u = new User("user" + id, "pass", "u" + id + "@test.com", "+237600000000", role);
        u.setId(id);
        return u;
    }

    private Trajet trajet(Long id, User chauffeur, StatutTrajet statut) {
        Vehicule v = new Vehicule("AB-1-CD", "Bus", 10, TypeDisposition.MINIBUS, chauffeur);
        v.setId(10L);
        Trajet t = new Trajet();
        t.setId(id);
        t.setChauffeur(chauffeur);
        t.setVehicule(v);
        t.setPointDepart("Yaoundé");
        t.setPointArrivee("Douala");
        t.setStatut(statut);
        t.setDateHeureDepart(OffsetDateTime.now().minusDays(1));
        t.setPrixParSiege(BigDecimal.valueOf(3500));
        return t;
    }

    private Reservation confirmedReservation(User passager, Trajet trajet) {
        Reservation r = new Reservation(passager, trajet, 3,
                com.mbarga.seat_reservation.reservation.ModePaiement.ESPECES,
                null, BigDecimal.valueOf(3500));
        r.setId(1L);
        r.setStatut(StatutReservation.CONFIRMEE);
        return r;
    }

    private AvisRequest request(Long trajetId) {
        AvisRequest r = new AvisRequest();
        r.setTrajetId(trajetId);
        r.setNote(4);
        r.setCommentaire("Très bon service");
        return r;
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_success() {
        User passager  = user(2L, Role.USER);
        User chauffeur = user(1L, Role.CHAUFFEUR);
        Trajet t = trajet(1L, chauffeur, StatutTrajet.TERMINE);
        Avis saved = new Avis(passager, chauffeur, t, 4, "Très bon service");
        saved.setId(1L);

        when(trajetRepository.findById(1L)).thenReturn(java.util.Optional.of(t));
        when(reservationRepository.findByPassagerId(2L)).thenReturn(List.of(confirmedReservation(passager, t)));
        when(avisRepository.existsByPassagerIdAndTrajetId(2L, 1L)).thenReturn(false);
        when(avisRepository.save(any())).thenReturn(saved);
        when(avisRepository.findByChauffeurId(1L)).thenReturn(List.of(saved));
        when(userRepository.save(any())).thenReturn(chauffeur);

        AvisResponse resp = service.create(request(1L), passager);

        assertThat(resp.getNote()).isEqualTo(4);
        assertThat(resp.getChauffeurId()).isEqualTo(1L);
        verify(avisRepository).save(any());
    }

    @Test
    void create_trajetNonTermine_throws() {
        User passager  = user(2L, Role.USER);
        User chauffeur = user(1L, Role.CHAUFFEUR);
        Trajet t = trajet(1L, chauffeur, StatutTrajet.EN_COURS);

        when(trajetRepository.findById(1L)).thenReturn(java.util.Optional.of(t));

        assertThatThrownBy(() -> service.create(request(1L), passager))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminé");
    }

    @Test
    void create_passagerNAyantPasReserve_throws() {
        User passager  = user(2L, Role.USER);
        User chauffeur = user(1L, Role.CHAUFFEUR);
        Trajet t = trajet(1L, chauffeur, StatutTrajet.TERMINE);

        when(trajetRepository.findById(1L)).thenReturn(java.util.Optional.of(t));
        when(reservationRepository.findByPassagerId(2L)).thenReturn(List.of()); // aucune réservation

        assertThatThrownBy(() -> service.create(request(1L), passager))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("effectué");
    }

    @Test
    void create_avisDejaExistant_throws() {
        User passager  = user(2L, Role.USER);
        User chauffeur = user(1L, Role.CHAUFFEUR);
        Trajet t = trajet(1L, chauffeur, StatutTrajet.TERMINE);

        when(trajetRepository.findById(1L)).thenReturn(java.util.Optional.of(t));
        when(reservationRepository.findByPassagerId(2L)).thenReturn(List.of(confirmedReservation(passager, t)));
        when(avisRepository.existsByPassagerIdAndTrajetId(2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(1L), passager))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà noté");
    }

    // ── getByChauffeur ────────────────────────────────────────────────────────

    @Test
    void getByChauffeur_retourneListe() {
        User passager  = user(2L, Role.USER);
        User chauffeur = user(1L, Role.CHAUFFEUR);
        Trajet t = trajet(1L, chauffeur, StatutTrajet.TERMINE);
        Avis a1 = new Avis(passager, chauffeur, t, 5, "Excellent");
        a1.setId(1L);
        Avis a2 = new Avis(passager, chauffeur, t, 3, "Correct");
        a2.setId(2L);

        when(avisRepository.findByChauffeurId(1L)).thenReturn(List.of(a1, a2));

        assertThat(service.getByChauffeur(1L)).hasSize(2);
    }
}