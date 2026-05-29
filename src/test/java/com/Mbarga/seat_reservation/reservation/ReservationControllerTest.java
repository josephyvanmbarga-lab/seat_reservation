package com.Mbarga.seat_reservation.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbarga.seat_reservation.auth.JwtAuthFilter;
import com.mbarga.seat_reservation.reservation.ReservationController;
import com.mbarga.seat_reservation.reservation.ReservationRequest;
import com.mbarga.seat_reservation.reservation.ReservationResponse;
import com.mbarga.seat_reservation.reservation.ReservationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ReservationService reservationService;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean UserDetailsService userDetailsService;

    @BeforeEach
    void bypassJwtFilter() throws Exception {
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(
                any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    private ReservationResponse response() {
        OffsetDateTime now = OffsetDateTime.now();
        return new ReservationResponse(1L, "Alice", 3, now, now.plusDays(1), 1L, "AB-123-CD");
    }

    @Test
    @WithMockUser
    void create_retourne201() throws Exception {
        ReservationRequest req = new ReservationRequest();
        req.setNomPassager("Alice");
        req.setSiegeNumero(3);
        req.setDateVoyage(OffsetDateTime.now().plusDays(1));
        req.setVehiculeId(1L);

        when(reservationService.create(any())).thenReturn(response());

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomPassager").value("Alice"))
                .andExpect(jsonPath("$.siegeNumero").value(3));
    }

    @Test
    @WithMockUser
    void getAll_retourne200() throws Exception {
        when(reservationService.getAll()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehiculeImmatriculation").value("AB-123-CD"));
    }

    @Test
    @WithMockUser
    void getById_retourne200() throws Exception {
        when(reservationService.getById(1L)).thenReturn(response());

        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void getByVehicule_retourne200() throws Exception {
        when(reservationService.getByVehicule(1L)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/reservations/vehicule/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    void cancel_retourne204() throws Exception {
        mockMvc.perform(delete("/api/reservations/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void create_siegeDejaReserve_retourne409() throws Exception {
        ReservationRequest req = new ReservationRequest();
        req.setNomPassager("Alice");
        req.setSiegeNumero(3);
        req.setDateVoyage(OffsetDateTime.now().plusDays(1));
        req.setVehiculeId(1L);

        when(reservationService.create(any()))
                .thenThrow(new IllegalStateException("Le siège 3 est déjà réservé pour ce voyage"));

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Le siège 3 est déjà réservé pour ce voyage"));
    }

    @Test
    @WithMockUser
    void create_vehiculeIntrouvable_retourne400() throws Exception {
        ReservationRequest req = new ReservationRequest();
        req.setNomPassager("Alice");
        req.setSiegeNumero(3);
        req.setDateVoyage(OffsetDateTime.now().plusDays(1));
        req.setVehiculeId(99L);

        when(reservationService.create(any()))
                .thenThrow(new IllegalArgumentException("Véhicule introuvable : id=99"));

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Véhicule introuvable : id=99"));
    }
}