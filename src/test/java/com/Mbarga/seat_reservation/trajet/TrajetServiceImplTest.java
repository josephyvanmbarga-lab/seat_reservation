package com.Mbarga.seat_reservation.trajet;

import com.mbarga.seat_reservation.auth.Role;
import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.tarification.HereMapsService;
import com.mbarga.seat_reservation.trajet.*;
import com.mbarga.seat_reservation.vehicule.TypeDisposition;
import com.mbarga.seat_reservation.vehicule.Vehicule;
import com.mbarga.seat_reservation.vehicule.VehiculeRepository;
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
class TrajetServiceImplTest {

    @Mock TrajetRepository   trajetRepository;
    @Mock VehiculeRepository vehiculeRepository;
    @Mock HereMapsService    hereMapsService;
    @InjectMocks TrajetServiceImpl service;

    // ── helpers ───────────────────────────────────────────────────────────────

    private User chauffeur(Long id) {
        User u = new User("chauffeur" + id, "pass", "c" + id + "@test.com", "+237600000000", Role.CHAUFFEUR);
        u.setId(id);
        return u;
    }

    private Vehicule vehicule(Long id, User chauffeur) {
        Vehicule v = new Vehicule("AB-" + id + "-CD", "Bus", 10, TypeDisposition.MINIBUS, chauffeur);
        v.setId(id);
        return v;
    }

    private Trajet trajet(Long id, User chauffeur, Vehicule vehicule, StatutTrajet statut) {
        Trajet t = new Trajet();
        t.setId(id);
        t.setChauffeur(chauffeur);
        t.setVehicule(vehicule);
        t.setPointDepart("Yaoundé");
        t.setLatDepart(BigDecimal.valueOf(3.848));
        t.setLngDepart(BigDecimal.valueOf(11.502));
        t.setPointArrivee("Douala");
        t.setLatArrivee(BigDecimal.valueOf(4.050));
        t.setLngArrivee(BigDecimal.valueOf(9.702));
        t.setDateHeureDepart(OffsetDateTime.now().plusDays(1));
        t.setPrixParSiege(BigDecimal.valueOf(3500));
        t.setStatut(statut);
        return t;
    }

    private TrajetRequest request(Long vehiculeId) {
        TrajetRequest r = new TrajetRequest();
        r.setVehiculeId(vehiculeId);
        r.setPointDepart("Yaoundé");
        r.setLatDepart(BigDecimal.valueOf(3.848));
        r.setLngDepart(BigDecimal.valueOf(11.502));
        r.setPointArrivee("Douala");
        r.setLatArrivee(BigDecimal.valueOf(4.050));
        r.setLngArrivee(BigDecimal.valueOf(9.702));
        r.setDateHeureDepart(OffsetDateTime.now().plusDays(1));
        return r;
    }

    private EstimationResponse estimation() {
        return new EstimationResponse(BigDecimal.valueOf(250), 22000, BigDecimal.valueOf(3500));
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_success() {
        User chauffeur = chauffeur(1L);
        Vehicule v     = vehicule(1L, chauffeur);
        Trajet saved   = trajet(1L, chauffeur, v, StatutTrajet.PLANIFIE);

        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(v));
        when(hereMapsService.estimer(any(), any(), any(), any())).thenReturn(estimation());
        when(trajetRepository.save(any())).thenReturn(saved);

        TrajetResponse resp = service.create(request(1L), chauffeur);

        assertThat(resp.getStatut()).isEqualTo(StatutTrajet.PLANIFIE);
        assertThat(resp.getPointDepart()).isEqualTo("Yaoundé");
        verify(trajetRepository).save(any());
    }

    @Test
    void create_vehiculeIntrouvable_throws() {
        when(vehiculeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(99L), chauffeur(1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_vehiculeNonProprietaire_throws() {
        User chauffeur = chauffeur(1L);
        User autre     = chauffeur(2L);
        Vehicule v     = vehicule(1L, chauffeur);
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> service.create(request(1L), autre))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("appartient");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_introuvable_throws() {
        when(trajetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // ── demarrer ──────────────────────────────────────────────────────────────

    @Test
    void demarrer_success() {
        User chauffeur = chauffeur(1L);
        Vehicule v = vehicule(1L, chauffeur);
        Trajet t = trajet(1L, chauffeur, v, StatutTrajet.PLANIFIE);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));
        when(trajetRepository.save(any())).thenReturn(t);

        TrajetResponse resp = service.demarrer(1L, chauffeur);

        assertThat(resp.getStatut()).isEqualTo(StatutTrajet.EN_COURS);
    }

    @Test
    void demarrer_trajetNonPlanifie_throws() {
        User chauffeur = chauffeur(1L);
        Vehicule v = vehicule(1L, chauffeur);
        Trajet t = trajet(1L, chauffeur, v, StatutTrajet.EN_COURS);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.demarrer(1L, chauffeur))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PLANIFIE");
    }

    // ── terminer ──────────────────────────────────────────────────────────────

    @Test
    void terminer_success() {
        User chauffeur = chauffeur(1L);
        Vehicule v = vehicule(1L, chauffeur);
        Trajet t = trajet(1L, chauffeur, v, StatutTrajet.EN_COURS);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));
        when(trajetRepository.save(any())).thenReturn(t);

        TrajetResponse resp = service.terminer(1L, chauffeur);

        assertThat(resp.getStatut()).isEqualTo(StatutTrajet.TERMINE);
    }

    @Test
    void terminer_trajetNonEnCours_throws() {
        User chauffeur = chauffeur(1L);
        Vehicule v = vehicule(1L, chauffeur);
        Trajet t = trajet(1L, chauffeur, v, StatutTrajet.PLANIFIE);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.terminer(1L, chauffeur))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EN_COURS");
    }

    // ── annuler ───────────────────────────────────────────────────────────────

    @Test
    void annuler_success() {
        User chauffeur = chauffeur(1L);
        Vehicule v = vehicule(1L, chauffeur);
        Trajet t = trajet(1L, chauffeur, v, StatutTrajet.PLANIFIE);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));
        when(trajetRepository.save(any())).thenReturn(t);

        TrajetResponse resp = service.annuler(1L, chauffeur);

        assertThat(resp.getStatut()).isEqualTo(StatutTrajet.ANNULE);
    }

    @Test
    void annuler_trajetDejaTermine_throws() {
        User chauffeur = chauffeur(1L);
        Vehicule v = vehicule(1L, chauffeur);
        Trajet t = trajet(1L, chauffeur, v, StatutTrajet.TERMINE);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.annuler(1L, chauffeur))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("terminé");
    }

    // ── getPlanSieges ─────────────────────────────────────────────────────────

    @Test
    void getPlanSieges_tousLibres() {
        User chauffeur = chauffeur(1L);
        Vehicule v = vehicule(1L, chauffeur);
        Trajet t = trajet(1L, chauffeur, v, StatutTrajet.PLANIFIE);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));
        when(trajetRepository.findSiegesReservesByTrajetId(1L)).thenReturn(List.of());

        List<SiegePlanResponse> plan = service.getPlanSieges(1L);

        assertThat(plan).hasSize(10);
        assertThat(plan).allMatch(s -> s.getStatut().equals("LIBRE"));
    }

    @Test
    void getPlanSieges_certainsReserves() {
        User chauffeur = chauffeur(1L);
        Vehicule v = vehicule(1L, chauffeur);
        Trajet t = trajet(1L, chauffeur, v, StatutTrajet.PLANIFIE);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(t));
        when(trajetRepository.findSiegesReservesByTrajetId(1L)).thenReturn(List.of(2, 5));

        List<SiegePlanResponse> plan = service.getPlanSieges(1L);

        assertThat(plan).hasSize(10);
        assertThat(plan.get(1).getStatut()).isEqualTo("RESERVE"); // siège 2
        assertThat(plan.get(4).getStatut()).isEqualTo("RESERVE"); // siège 5
        assertThat(plan.get(0).getStatut()).isEqualTo("LIBRE");   // siège 1
    }
}