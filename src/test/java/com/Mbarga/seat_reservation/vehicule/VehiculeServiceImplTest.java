package com.Mbarga.seat_reservation.vehicule;

import com.mbarga.seat_reservation.reservation.ReservationRepository;
import com.mbarga.seat_reservation.vehicule.Vehicule;
import com.mbarga.seat_reservation.vehicule.VehiculeRepository;
import com.mbarga.seat_reservation.vehicule.VehiculeRequest;
import com.mbarga.seat_reservation.vehicule.VehiculeResponse;
import com.mbarga.seat_reservation.vehicule.VehiculeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceImplTest {

    @Mock VehiculeRepository repository;
    @Mock ReservationRepository reservationRepository;
    @InjectMocks VehiculeServiceImpl service;

    @Test
    void create_success() {
        VehiculeRequest req = new VehiculeRequest();
        req.setImmatriculation("AB-123-CD");
        req.setModele("Mercedes");
        req.setCapacite(30);

        when(repository.existsByImmatriculation("AB-123-CD")).thenReturn(false);
        when(repository.save(any())).thenReturn(new Vehicule("AB-123-CD", "Mercedes", 30));

        VehiculeResponse resp = service.create(req);

        assertThat(resp.getImmatriculation()).isEqualTo("AB-123-CD");
        assertThat(resp.getCapacite()).isEqualTo(30);
    }

    @Test
    void create_immatriculationVide_throws() {
        VehiculeRequest req = new VehiculeRequest();
        req.setImmatriculation("  ");
        req.setCapacite(10);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("immatriculation");
    }

    @Test
    void create_capaciteZero_throws() {
        VehiculeRequest req = new VehiculeRequest();
        req.setImmatriculation("AB-123-CD");
        req.setCapacite(0);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capacité");
    }

    @Test
    void create_immatriculationDupliquee_throws() {
        VehiculeRequest req = new VehiculeRequest();
        req.setImmatriculation("AB-123-CD");
        req.setCapacite(10);

        when(repository.existsByImmatriculation("AB-123-CD")).thenReturn(true);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getById_introuvable_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_success() {
        Vehicule existing = new Vehicule("AB-123-CD", "Bus", 20);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByImmatriculation("XY-999-ZZ")).thenReturn(false);
        when(repository.save(any())).thenReturn(new Vehicule("XY-999-ZZ", "Van", 15));

        VehiculeRequest req = new VehiculeRequest();
        req.setImmatriculation("XY-999-ZZ");
        req.setModele("Van");
        req.setCapacite(15);

        VehiculeResponse resp = service.update(1L, req);

        assertThat(resp.getImmatriculation()).isEqualTo("XY-999-ZZ");
        assertThat(resp.getCapacite()).isEqualTo(15);
    }

    @Test
    void delete_introuvable_throws() {
        when(repository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5");
    }

    @Test
    void delete_success() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void getSiegesDisponibles_tousLibres() {
        Vehicule v = new Vehicule("AB-123-CD", "Bus", 5);
        OffsetDateTime date = OffsetDateTime.now().plusDays(1);
        when(repository.findById(1L)).thenReturn(Optional.of(v));
        when(reservationRepository.findSiegesReservesByVehiculeIdAndDateVoyage(1L, date))
                .thenReturn(List.of());

        List<Integer> disponibles = service.getSiegesDisponibles(1L, date);

        assertThat(disponibles).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void getSiegesDisponibles_certainsPris() {
        Vehicule v = new Vehicule("AB-123-CD", "Bus", 5);
        OffsetDateTime date = OffsetDateTime.now().plusDays(1);
        when(repository.findById(1L)).thenReturn(Optional.of(v));
        when(reservationRepository.findSiegesReservesByVehiculeIdAndDateVoyage(1L, date))
                .thenReturn(List.of(2, 4));

        List<Integer> disponibles = service.getSiegesDisponibles(1L, date);

        assertThat(disponibles).containsExactly(1, 3, 5);
    }

    @Test
    void getSiegesDisponibles_tousOccupes() {
        Vehicule v = new Vehicule("AB-123-CD", "Bus", 3);
        OffsetDateTime date = OffsetDateTime.now().plusDays(1);
        when(repository.findById(1L)).thenReturn(Optional.of(v));
        when(reservationRepository.findSiegesReservesByVehiculeIdAndDateVoyage(1L, date))
                .thenReturn(List.of(1, 2, 3));

        List<Integer> disponibles = service.getSiegesDisponibles(1L, date);

        assertThat(disponibles).isEmpty();
    }

    @Test
    void getSiegesDisponibles_vehiculeIntrouvable_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSiegesDisponibles(99L, OffsetDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }
}