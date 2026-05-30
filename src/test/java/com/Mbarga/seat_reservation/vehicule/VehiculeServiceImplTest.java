package com.Mbarga.seat_reservation.vehicule;

import com.mbarga.seat_reservation.auth.Role;
import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.vehicule.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceImplTest {

    @Mock VehiculeRepository repository;
    @InjectMocks VehiculeServiceImpl service;

    private User user(Long id, Role role) {
        User u = new User("user" + id, "pass", "user" + id + "@test.com", "+237600000000", role);
        u.setId(id);
        return u;
    }

    private Vehicule vehicule(Long id, User chauffeur) {
        Vehicule v = new Vehicule("AB-" + id + "-CD", "Bus", 10, TypeDisposition.MINIBUS, chauffeur);
        v.setId(id);
        return v;
    }

    private VehiculeRequest request(String immat) {
        VehiculeRequest r = new VehiculeRequest();
        r.setImmatriculation(immat);
        r.setModele("Bus");
        r.setCapacite(20);
        r.setTypeDisposition(TypeDisposition.MINIBUS);
        return r;
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_success() {
        User chauffeur = user(1L, Role.CHAUFFEUR);
        Vehicule saved  = vehicule(1L, chauffeur);
        when(repository.existsByImmatriculation("AB-123-CD")).thenReturn(false);
        when(repository.save(any())).thenReturn(saved);

        VehiculeResponse resp = service.create(request("AB-123-CD"), chauffeur);

        assertThat(resp.getImmatriculation()).isEqualTo("AB-1-CD");
        assertThat(resp.getChauffeurId()).isEqualTo(1L);
    }

    @Test
    void create_immatriculationDupliquee_throws() {
        when(repository.existsByImmatriculation("AB-123-CD")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("AB-123-CD"), user(1L, Role.CHAUFFEUR)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("immatriculation");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_introuvable_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getById_success() {
        User chauffeur = user(1L, Role.CHAUFFEUR);
        when(repository.findById(1L)).thenReturn(Optional.of(vehicule(1L, chauffeur)));

        VehiculeResponse resp = service.getById(1L);

        assertThat(resp.getId()).isEqualTo(1L);
    }

    // ── getMesVehicules ───────────────────────────────────────────────────────

    @Test
    void getMesVehicules_retourneListe() {
        User chauffeur = user(1L, Role.CHAUFFEUR);
        when(repository.findByChauffeurId(1L))
                .thenReturn(List.of(vehicule(1L, chauffeur), vehicule(2L, chauffeur)));

        assertThat(service.getMesVehicules(chauffeur)).hasSize(2);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_parAdmin_success() {
        User admin    = user(99L, Role.ADMIN);
        User chauffeur = user(1L, Role.CHAUFFEUR);
        Vehicule existing = vehicule(1L, chauffeur);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByImmatriculationAndIdNot("XY-999-ZZ", 1L)).thenReturn(false);
        when(repository.save(any())).thenReturn(existing);

        VehiculeResponse resp = service.update(1L, request("XY-999-ZZ"), admin);

        assertThat(resp).isNotNull();
        verify(repository).save(any());
    }

    @Test
    void update_parProprietaire_success() {
        User chauffeur = user(1L, Role.CHAUFFEUR);
        Vehicule existing = vehicule(1L, chauffeur);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByImmatriculationAndIdNot("XY-999-ZZ", 1L)).thenReturn(false);
        when(repository.save(any())).thenReturn(existing);

        service.update(1L, request("XY-999-ZZ"), chauffeur);

        verify(repository).save(any());
    }

    @Test
    void update_nonProprietaire_throws() {
        User chauffeur = user(1L, Role.CHAUFFEUR);
        User autre     = user(2L, Role.CHAUFFEUR);
        when(repository.findById(1L)).thenReturn(Optional.of(vehicule(1L, chauffeur)));

        assertThatThrownBy(() -> service.update(1L, request("XY"), autre))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("appartient");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_parProprietaire_success() {
        User chauffeur = user(1L, Role.CHAUFFEUR);
        when(repository.findById(1L)).thenReturn(Optional.of(vehicule(1L, chauffeur)));

        service.delete(1L, chauffeur);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_nonProprietaire_throws() {
        User chauffeur = user(1L, Role.CHAUFFEUR);
        User autre     = user(2L, Role.CHAUFFEUR);
        when(repository.findById(1L)).thenReturn(Optional.of(vehicule(1L, chauffeur)));

        assertThatThrownBy(() -> service.delete(1L, autre))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("appartient");
    }

    @Test
    void delete_introuvable_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, user(1L, Role.ADMIN)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}