package com.Mbarga.seat_reservation.vehicule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbarga.seat_reservation.auth.JwtService;
import com.mbarga.seat_reservation.auth.Role;
import com.mbarga.seat_reservation.auth.User;
import com.mbarga.seat_reservation.vehicule.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehiculeController.class)
@Import(VehiculeControllerTest.TestSecConfig.class)
class VehiculeControllerTest {

    /**
     * Remplace la SecurityFilterChain principale (STATELESS) par une chaîne
     * avec session activée, pour que le post-processeur authentication() fonctionne.
     * @Order(0) garantit la priorité maximale sur la chaîne réelle.
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
                    .authorizeHttpRequests(a -> a
                            .requestMatchers(HttpMethod.POST,   "/api/vehicules").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT,    "/api/vehicules/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/vehicules/**").hasRole("ADMIN")
                            .anyRequest().authenticated()
                    )
                    .exceptionHandling(e -> e
                            .authenticationEntryPoint((req, res, ex) ->
                                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                    )
                    .build();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @MockBean JwtService         jwtService;
    @MockBean UserDetailsService userDetailsService;
    @MockBean VehiculeService    service;

    // ── helpers ───────────────────────────────────────────────────────────────

    private User user(Long id, Role role) {
        User u = new User("user" + id, "pass", "u" + id + "@test.com", "+237600000000", role);
        u.setId(id);
        return u;
    }

    private UsernamePasswordAuthenticationToken auth(User u) {
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    private VehiculeResponse response(Long id) {
        return new VehiculeResponse(id, "AB-" + id + "-CD", "Bus", 10,
                TypeDisposition.MINIBUS, 1L, "chauffeur1");
    }

    private VehiculeRequest request() {
        VehiculeRequest r = new VehiculeRequest();
        r.setImmatriculation("AB-1-CD");
        r.setModele("Bus");
        r.setCapacite(20);
        r.setTypeDisposition(TypeDisposition.MINIBUS);
        return r;
    }

    // ── POST /api/vehicules ───────────────────────────────────────────────────

    @Test
    void create_parAdmin_returns201() throws Exception {
        User admin = user(99L, Role.ADMIN);
        when(service.create(any(), any())).thenReturn(response(1L));

        mvc.perform(post("/api/vehicules")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.immatriculation").value("AB-1-CD"));
    }

    @Test
    void create_sansRoleAdmin_returns403() throws Exception {
        User passager = user(2L, Role.USER);

        mvc.perform(post("/api/vehicules")
                        .with(authentication(auth(passager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_validationEchouee_returns400() throws Exception {
        User admin = user(99L, Role.ADMIN);
        VehiculeRequest bad = new VehiculeRequest(); // immatriculation absente

        mvc.perform(post("/api/vehicules")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_immatDupliquee_returns409() throws Exception {
        User admin = user(99L, Role.ADMIN);
        when(service.create(any(), any()))
                .thenThrow(new IllegalStateException("immatriculation déjà utilisée"));

        mvc.perform(post("/api/vehicules")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("immatriculation déjà utilisée"));
    }

    // ── GET /api/vehicules/{id} ───────────────────────────────────────────────

    @Test
    void getById_success() throws Exception {
        User u = user(1L, Role.USER);
        when(service.getById(1L)).thenReturn(response(1L));

        mvc.perform(get("/api/vehicules/1").with(authentication(auth(u))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_nonAuthentifie_returns401() throws Exception {
        mvc.perform(get("/api/vehicules/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_introuvable_returns400() throws Exception {
        User u = user(1L, Role.USER);
        when(service.getById(99L))
                .thenThrow(new IllegalArgumentException("Véhicule 99 introuvable"));

        mvc.perform(get("/api/vehicules/99").with(authentication(auth(u))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Véhicule 99 introuvable"));
    }

    // ── GET /api/vehicules ────────────────────────────────────────────────────

    @Test
    void getAll_retourneListe() throws Exception {
        User u = user(1L, Role.USER);
        when(service.getAll()).thenReturn(List.of(response(1L), response(2L)));

        mvc.perform(get("/api/vehicules").with(authentication(auth(u))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── GET /api/vehicules/mes-vehicules ──────────────────────────────────────

    @Test
    void getMesVehicules_retourneListe() throws Exception {
        User chauffeur = user(1L, Role.CHAUFFEUR);
        when(service.getMesVehicules(any())).thenReturn(List.of(response(1L)));

        mvc.perform(get("/api/vehicules/mes-vehicules").with(authentication(auth(chauffeur))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── PUT /api/vehicules/{id} ───────────────────────────────────────────────

    @Test
    void update_parAdmin_returns200() throws Exception {
        User admin = user(99L, Role.ADMIN);
        when(service.update(eq(1L), any(), any())).thenReturn(response(1L));

        mvc.perform(put("/api/vehicules/1")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request())))
                .andExpect(status().isOk());
    }

    @Test
    void update_sansRoleAdmin_returns403() throws Exception {
        User passager = user(2L, Role.USER);

        mvc.perform(put("/api/vehicules/1")
                        .with(authentication(auth(passager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request())))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/vehicules/{id} ────────────────────────────────────────────

    @Test
    void delete_parAdmin_returns204() throws Exception {
        User admin = user(99L, Role.ADMIN);
        doNothing().when(service).delete(eq(1L), any());

        mvc.perform(delete("/api/vehicules/1").with(authentication(auth(admin))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_sansRoleAdmin_returns403() throws Exception {
        User passager = user(2L, Role.USER);

        mvc.perform(delete("/api/vehicules/1").with(authentication(auth(passager))))
                .andExpect(status().isForbidden());
    }
}
