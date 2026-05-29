package com.Mbarga.seat_reservation.reservation;

import com.mbarga.seat_reservation.reservation.Reservation;
import com.mbarga.seat_reservation.reservation.ReservationEventProducer;
import com.mbarga.seat_reservation.reservation.ReservationRepository;
import com.mbarga.seat_reservation.reservation.ReservationRequest;
import com.mbarga.seat_reservation.reservation.ReservationResponse;
import com.mbarga.seat_reservation.reservation.ReservationServiceImpl;
import com.mbarga.seat_reservation.vehicule.Vehicule;
import com.mbarga.seat_reservation.vehicule.VehiculeRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock ReservationRepository reservationRepository;
    @Mock VehiculeRepository vehiculeRepository;
    @Mock ReservationEventProducer eventProducer;
    @InjectMocks ReservationServiceImpl service;

    private static final OffsetDateTime DATE = OffsetDateTime.now().plusDays(1);

    private Vehicule vehicule() {
        return new Vehicule("AB-123-CD", "Bus", 10);
    }

    @Test
    void create_success() {
        Vehicule v = vehicule();
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(v));
        when(reservationRepository.existsByVehiculeIdAndDateVoyageAndSiegeNumero(any(), eq(DATE), eq(3)))
                .thenReturn(false);
        when(reservationRepository.save(any())).thenReturn(new Reservation("Alice", 3, DATE, v));
        doNothing().when(eventProducer).publish(any());

        ReservationRequest req = request("Alice", 3, DATE, 1L);
        ReservationResponse resp = service.create(req);

        assertThat(resp.getNomPassager()).isEqualTo("Alice");
        assertThat(resp.getSiegeNumero()).isEqualTo(3);
        verify(eventProducer).publish(any());
    }

    @Test
    void create_nomPassagerVide_throws() {
        assertThatThrownBy(() -> service.create(request("  ", 1, DATE, 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom du passager");
    }

    @Test
    void create_dateVoyageNull_throws() {
        assertThatThrownBy(() -> service.create(request("Alice", 1, null, 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("date de voyage");
    }

    @Test
    void create_vehiculeIntrouvable_throws() {
        when(vehiculeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request("Alice", 1, DATE, 99L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_siegeHorsCapacite_throws() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule())); // capacite=10

        assertThatThrownBy(() -> service.create(request("Alice", 11, DATE, 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Siège invalide");
    }

    @Test
    void create_siegeZero_throws() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule()));

        assertThatThrownBy(() -> service.create(request("Alice", 0, DATE, 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Siège invalide");
    }

    @Test
    void create_siegeDejaReserve_throws() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule()));
        when(reservationRepository.existsByVehiculeIdAndDateVoyageAndSiegeNumero(any(), eq(DATE), eq(3)))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(request("Alice", 3, DATE, 1L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3");
    }

    @Test
    void cancel_success() {
        when(reservationRepository.existsById(1L)).thenReturn(true);

        service.cancel(1L);

        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void cancel_introuvable_throws() {
        when(reservationRepository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.cancel(5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5");
    }

    @Test
    void getById_introuvable_throws() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAll_retourneTout() {
        Vehicule v = vehicule();
        when(reservationRepository.findAll())
                .thenReturn(List.of(
                        new Reservation("Alice", 1, DATE, v),
                        new Reservation("Bob", 2, DATE, v)
                ));

        assertThat(service.getAll()).hasSize(2);
    }

    @Test
    void getByVehicule_vehiculeIntrouvable_throws() {
        when(vehiculeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getByVehicule(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getByVehicule_retourneListe() {
        Vehicule v = vehicule();
        when(vehiculeRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.findByVehiculeId(1L))
                .thenReturn(List.of(new Reservation("Alice", 1, DATE, v)));

        assertThat(service.getByVehicule(1L)).hasSize(1);
    }

    private ReservationRequest request(String nom, int siege, OffsetDateTime date, Long vehiculeId) {
        ReservationRequest r = new ReservationRequest();
        r.setNomPassager(nom);
        r.setSiegeNumero(siege);
        r.setDateVoyage(date);
        r.setVehiculeId(vehiculeId);
        return r;
    }
}