package com.Mbarga.seat_reservation.reservation;

import com.mbarga.seat_reservation.auth.Role;
import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.reservation.*;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock ReservationRepository  reservationRepository;
    @Mock TrajetRepository       trajetRepository;
    @Mock ReservationEventProducer eventProducer;
    @InjectMocks ReservationServiceImpl service;

    // ── helpers ───────────────────────────────────────────────────────────────

    private User user(Long id, Role role) {
        User u = new User("user" + id, "pass", "u" + id + "@test.com", "+237600000000", role);
        u.setId(id);
        return u;
    }

    private Trajet trajet(StatutTrajet statut, OffsetDateTime depart) {
        User chauffeur = user(10L, Role.CHAUFFEUR);
        Vehicule v = new Vehicule("AB-1-CD", "Bus", 10, TypeDisposition.MINIBUS, chauffeur);
        v.setId(10L);
        Trajet t = new Trajet();
        t.setId(1L);
        t.setChauffeur(chauffeur);
        t.setVehicule(v);
        t.setPointDepart("Yaoundé");
        t.setLatDepart(BigDecimal.valueOf(3.848));
        t.setLngDepart(BigDecimal.valueOf(11.502));
        t.setPointArrivee("Douala");
        t.setLatArrivee(BigDecimal.valueOf(4.050));
        t.setLngArrivee(BigDecimal.valueOf(9.702));
        t.setDateHeureDepart(depart);
        t.setPrixParSiege(BigDecimal.valueOf(3500));
        t.setStatut(statut);
        return t;
    }

    private Reservation reservation(User passager, Trajet trajet) {
        Reservation r = new Reservation(passager, trajet, 3,
                ModePaiement.ESPECES, null, BigDecimal.valueOf(3500));
        r.setId(1L);
        return r;
    }

    private ReservationRequest request(Long trajetId, int siege, ModePaiement mode,
                                        OperateurMobileMoney operateur) {
        ReservationRequest r = new ReservationRequest();
        r.setTrajetId(trajetId);
        r.setSiegeNumero(siege);
        r.setModePaiement(mode);
        r.setOperateurMobileMoney(operateur);
        return r;
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_success() {
        User passager = user(2L, Role.USER);
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        Reservation saved = reservation(passager, t);

        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));
        when(reservationRepository.existsByTrajetIdAndSiegeNumeroAndStatut(
                1L, 3, StatutReservation.CONFIRMEE)).thenReturn(false);
        when(reservationRepository.save(any())).thenReturn(saved);
        when(trajetRepository.save(any())).thenReturn(t);
        doNothing().when(eventProducer).publish(any());

        ReservationResponse resp = service.create(request(1L, 3, ModePaiement.ESPECES, null), passager);

        assertThat(resp.getSiegeNumero()).isEqualTo(3);
        assertThat(resp.getPassagerId()).isEqualTo(2L);
        verify(eventProducer).publish(any());
        verify(trajetRepository).save(t);
    }

    @Test
    void create_trajetIntrouvable_throws() {
        when(trajetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(99L, 1, ModePaiement.ESPECES, null), user(1L, Role.USER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_trajetNonPlanifie_throws() {
        Trajet t = trajet(StatutTrajet.EN_COURS, OffsetDateTime.now().plusDays(1));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.create(request(1L, 1, ModePaiement.ESPECES, null), user(1L, Role.USER)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("réservations");
    }

    @Test
    void create_dateDepassee_throws() {
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().minusHours(1));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.create(request(1L, 1, ModePaiement.ESPECES, null), user(1L, Role.USER)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("départ");
    }

    @Test
    void create_siegeHorsCapacite_throws() {
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.create(request(1L, 11, ModePaiement.ESPECES, null), user(1L, Role.USER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Siège invalide");
    }

    @Test
    void create_siegeDejaReserve_throws() {
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));
        when(reservationRepository.existsByTrajetIdAndSiegeNumeroAndStatut(
                1L, 3, StatutReservation.CONFIRMEE)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(1L, 3, ModePaiement.ESPECES, null), user(1L, Role.USER)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3");
    }

    @Test
    void create_mobileMoneySansOperateur_throws() {
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));
        when(reservationRepository.existsByTrajetIdAndSiegeNumeroAndStatut(any(), anyInt(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.create(request(1L, 3, ModePaiement.MOBILE_MONEY, null), user(1L, Role.USER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("opérateur");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_enTantQuAdmin_success() {
        User admin = user(99L, Role.ADMIN);
        User passager = user(2L, Role.USER);
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation(passager, t)));

        ReservationResponse resp = service.getById(1L, admin);

        assertThat(resp.getId()).isEqualTo(1L);
    }

    @Test
    void getById_enTantQuePassager_success() {
        User passager = user(2L, Role.USER);
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation(passager, t)));

        ReservationResponse resp = service.getById(1L, passager);

        assertThat(resp.getPassagerId()).isEqualTo(2L);
    }

    @Test
    void getById_autreUser_throws() {
        User passager = user(2L, Role.USER);
        User autre    = user(3L, Role.USER);
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation(passager, t)));

        assertThatThrownBy(() -> service.getById(1L, autre))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Accès");
    }

    // ── cancel ────────────────────────────────────────────────────────────────

    @Test
    void cancel_parPassager_success() {
        User passager = user(2L, Role.USER);
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        Reservation r = reservation(passager, t);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any())).thenReturn(r);
        when(trajetRepository.save(any())).thenReturn(t);

        service.cancel(1L, passager);

        assertThat(r.getStatut()).isEqualTo(StatutReservation.ANNULEE);
    }

    @Test
    void cancel_autreUser_throws() {
        User passager = user(2L, Role.USER);
        User autre    = user(3L, Role.USER);
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation(passager, t)));

        assertThatThrownBy(() -> service.cancel(1L, autre))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("annuler");
    }

    @Test
    void cancel_dejaAnnulee_throws() {
        User passager = user(2L, Role.USER);
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        Reservation r = reservation(passager, t);
        r.setStatut(StatutReservation.ANNULEE);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.cancel(1L, passager))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("annulée");
    }

    // ── getAll / getMesReservations / getByTrajet ─────────────────────────────

    @Test
    void getAll_retourneTout() {
        User passager = user(2L, Role.USER);
        Trajet t = trajet(StatutTrajet.PLANIFIE, OffsetDateTime.now().plusDays(1));
        when(reservationRepository.findAll())
                .thenReturn(List.of(reservation(passager, t), reservation(passager, t)));

        assertThat(service.getAll()).hasSize(2);
    }

    @Test
    void getByTrajet_trajetIntrouvable_throws() {
        when(trajetRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getByTrajet(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}