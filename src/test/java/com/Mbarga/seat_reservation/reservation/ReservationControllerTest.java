package com.Mbarga.seat_reservation.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbarga.seat_reservation.auth.JwtService;
import com.mbarga.seat_reservation.auth.Role;
import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.reservation.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@Import(ReservationControllerTest.TestSecConfig.class)
class ReservationControllerTest {

    /**
     * Remplace la SecurityFilterChain STATELESS par une chaîne avec session,
     * afin que le post-processeur authentication() fonctionne correctement.
     */
    @TestConfiguration
    static class TestSecConfig {
        @Bean
        @Order(0)
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .securityContext(ctx -> ctx
                            .securityContextRepository(new HttpSessionSecurityContextRepository()))
                    .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                    .exceptionHandling(e -> e
                            .authenticationEntryPoint((req, res, ex) ->
                                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                    )
                    .build();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @MockBean JwtService          jwtService;
    @MockBean UserDetailsService  userDetailsService;
    @MockBean ReservationService  service;

    // ── helpers ───────────────────────────────────────────────────────────────

    private User user(Long id, Role role) {
        User u = new User("user" + id, "pass", "u" + id + "@test.com", "+237600000000", role);
        u.setId(id);
        return u;
    }

    private UsernamePasswordAuthenticationToken auth(User u) {
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    private ReservationResponse response(Long id) {
        return new ReservationResponse(
                id, 2L, "passager2", 1L, "Yaoundé", "Douala",
                OffsetDateTime.now().plusDays(1), 3,
                StatutReservation.CONFIRMEE, ModePaiement.ESPECES, null,
                BigDecimal.valueOf(3500), OffsetDateTime.now()
        );
    }

    private ReservationRequest request() {
        ReservationRequest r = new ReservationRequest();
        r.setTrajetId(1L);
        r.setSiegeNumero(3);
        r.setModePaiement(ModePaiement.ESPECES);
        return r;
    }

    // ── POST /api/reservations ────────────────────────────────────────────────

    @Test
    void create_success_returns201() throws Exception {
        User passager = user(2L, Role.USER);
        when(service.create(any(), any())).thenReturn(response(1L));

        mvc.perform(post("/api/reservations")
                        .with(authentication(auth(passager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.siegeNumero").value(3))
                .andExpect(jsonPath("$.passagerId").value(2));
    }

    @Test
    void create_validationEchouee_returns400() throws Exception {
        User passager = user(2L, Role.USER);
        ReservationRequest bad = new ReservationRequest(); // trajetId null, modePaiement null

        mvc.perform(post("/api/reservations")
                        .with(authentication(auth(passager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_siegeDejaReserve_returns409() throws Exception {
        User passager = user(2L, Role.USER);
        when(service.create(any(), any()))
                .thenThrow(new IllegalStateException("Le siège 3 est déjà réservé"));

        mvc.perform(post("/api/reservations")
                        .with(authentication(auth(passager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Le siège 3 est déjà réservé"));
    }

    @Test
    void create_trajetIntrouvable_returns400() throws Exception {
        User passager = user(2L, Role.USER);
        when(service.create(any(), any()))
                .thenThrow(new IllegalArgumentException("Trajet 99 introuvable"));

        mvc.perform(post("/api/reservations")
                        .with(authentication(auth(passager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Trajet 99 introuvable"));
    }

    // ── GET /api/reservations/{id} ────────────────────────────────────────────

    @Test
    void getById_success() throws Exception {
        User passager = user(2L, Role.USER);
        when(service.getById(eq(1L), any())).thenReturn(response(1L));

        mvc.perform(get("/api/reservations/1").with(authentication(auth(passager))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_nonAuthentifie_returns401() throws Exception {
        mvc.perform(get("/api/reservations/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_accesRefuse_returns409() throws Exception {
        User autre = user(3L, Role.USER);
        when(service.getById(eq(1L), any()))
                .thenThrow(new IllegalStateException("Accès refusé à cette réservation"));

        mvc.perform(get("/api/reservations/1").with(authentication(auth(autre))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Accès refusé à cette réservation"));
    }

    // ── GET /api/reservations ─────────────────────────────────────────────────

    @Test
    void getAll_retourneListe() throws Exception {
        User admin = user(99L, Role.ADMIN);
        when(service.getAll()).thenReturn(List.of(response(1L), response(2L)));

        mvc.perform(get("/api/reservations").with(authentication(auth(admin))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── GET /api/reservations/mes-reservations ────────────────────────────────

    @Test
    void getMesReservations_retourneListe() throws Exception {
        User passager = user(2L, Role.USER);
        when(service.getMesReservations(any())).thenReturn(List.of(response(1L)));

        mvc.perform(get("/api/reservations/mes-reservations").with(authentication(auth(passager))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/reservations/trajet/{trajetId} ───────────────────────────────

    @Test
    void getByTrajet_retourneListe() throws Exception {
        User chauffeur = user(1L, Role.CHAUFFEUR);
        when(service.getByTrajet(1L)).thenReturn(List.of(response(1L)));

        mvc.perform(get("/api/reservations/trajet/1").with(authentication(auth(chauffeur))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/reservations/mes-trajets ─────────────────────────────────────

    @Test
    void getMesTrajets_retourneListe() throws Exception {
        User chauffeur = user(1L, Role.CHAUFFEUR);
        when(service.getMesTrajetsReservations(any())).thenReturn(List.of(response(1L)));

        mvc.perform(get("/api/reservations/mes-trajets").with(authentication(auth(chauffeur))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── DELETE /api/reservations/{id} ─────────────────────────────────────────

    @Test
    void cancel_success_returns204() throws Exception {
        User passager = user(2L, Role.USER);
        doNothing().when(service).cancel(eq(1L), any());

        mvc.perform(delete("/api/reservations/1").with(authentication(auth(passager))))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancel_nonProprietaire_returns409() throws Exception {
        User autre = user(3L, Role.USER);
        doThrow(new IllegalStateException("Vous ne pouvez pas annuler cette réservation"))
                .when(service).cancel(eq(1L), any());

        mvc.perform(delete("/api/reservations/1").with(authentication(auth(autre))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Vous ne pouvez pas annuler cette réservation"));
    }

    // ── PATCH /api/reservations/{id}/statut ───────────────────────────────────

    @Test
    void updateStatut_success() throws Exception {
        User admin = user(99L, Role.ADMIN);
        when(service.updateStatut(eq(1L), eq(StatutReservation.CONFIRMEE))).thenReturn(response(1L));

        mvc.perform(patch("/api/reservations/1/statut")
                        .with(authentication(auth(admin)))
                        .param("statut", "CONFIRMEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CONFIRMEE"));
    }
}
