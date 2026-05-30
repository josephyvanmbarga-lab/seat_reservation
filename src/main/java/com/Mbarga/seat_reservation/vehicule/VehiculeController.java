package com.mbarga.seat_reservation.vehicule;

import com.mbarga.seat_reservation.auth.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VehiculeResponse> create(@Valid @RequestBody VehiculeRequest request,
                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<VehiculeResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/mes-vehicules")
    public ResponseEntity<List<VehiculeResponse>> getMesVehicules(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getMesVehicules(currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculeResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody VehiculeRequest request,
                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.update(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal User currentUser) {
        service.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
