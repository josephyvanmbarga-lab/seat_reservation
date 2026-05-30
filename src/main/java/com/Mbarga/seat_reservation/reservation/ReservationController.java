package com.mbarga.seat_reservation.reservation;

import com.mbarga.seat_reservation.auth.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request,
                                                       @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id,
                                                        @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getById(id, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/mes-reservations")
    public ResponseEntity<List<ReservationResponse>> getMesReservations(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getMesReservations(currentUser));
    }

    @GetMapping("/trajet/{trajetId}")
    public ResponseEntity<List<ReservationResponse>> getByTrajet(@PathVariable Long trajetId) {
        return ResponseEntity.ok(service.getByTrajet(trajetId));
    }

    @GetMapping("/mes-trajets")
    public ResponseEntity<List<ReservationResponse>> getMesTrajetsReservations(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getMesTrajetsReservations(currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id,
                                        @AuthenticationPrincipal User currentUser) {
        service.cancel(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<ReservationResponse> updateStatut(@PathVariable Long id,
                                                             @RequestParam StatutReservation statut) {
        return ResponseEntity.ok(service.updateStatut(id, statut));
    }
}
