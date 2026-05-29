package com.Mbarga.seat_reservation.vehicule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbarga.seat_reservation.auth.JwtAuthFilter;
import com.mbarga.seat_reservation.vehicule.VehiculeController;
import com.mbarga.seat_reservation.vehicule.VehiculeRequest;
import com.mbarga.seat_reservation.vehicule.VehiculeResponse;
import com.mbarga.seat_reservation.vehicule.VehiculeService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehiculeController.class)
class VehiculeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean VehiculeService vehiculeService;
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

    @Test
    @WithMockUser
    void getAll_retourne200() throws Exception {
        when(vehiculeService.getAll()).thenReturn(List.of(
                new VehiculeResponse(1L, "AB-123-CD", "Bus", 30)
        ));

        mockMvc.perform(get("/api/vehicules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].immatriculation").value("AB-123-CD"));
    }

    @Test
    @WithMockUser
    void getById_retourne200() throws Exception {
        when(vehiculeService.getById(1L))
                .thenReturn(new VehiculeResponse(1L, "AB-123-CD", "Bus", 30));

        mockMvc.perform(get("/api/vehicules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacite").value(30));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_enTantQuAdmin_retourne201() throws Exception {
        VehiculeRequest req = new VehiculeRequest();
        req.setImmatriculation("AB-123-CD");
        req.setModele("Bus");
        req.setCapacite(30);

        when(vehiculeService.create(any()))
                .thenReturn(new VehiculeResponse(1L, "AB-123-CD", "Bus", 30));

        mockMvc.perform(post("/api/vehicules")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_enTantQuAdmin_retourne200() throws Exception {
        VehiculeRequest req = new VehiculeRequest();
        req.setImmatriculation("XY-999-ZZ");
        req.setModele("Van");
        req.setCapacite(15);

        when(vehiculeService.update(eq(1L), any()))
                .thenReturn(new VehiculeResponse(1L, "XY-999-ZZ", "Van", 15));

        mockMvc.perform(put("/api/vehicules/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.immatriculation").value("XY-999-ZZ"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_enTantQuAdmin_retourne204() throws Exception {
        mockMvc.perform(delete("/api/vehicules/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void getSiegesDisponibles_retourne200() throws Exception {
        when(vehiculeService.getSiegesDisponibles(eq(1L), any()))
                .thenReturn(List.of(1, 3, 5));

        mockMvc.perform(get("/api/vehicules/1/sieges-disponibles")
                        .param("date", "2025-06-15T08:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(3))
                .andExpect(jsonPath("$[2]").value(5));
    }
}