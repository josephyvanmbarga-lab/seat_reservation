package com.mbarga.seat_reservation.avis;

import com.mbarga.seat_reservation.auth.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avis")
public class AvisController {

    private final AvisService service;

    public AvisController(AvisService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AvisResponse> create(@Valid @RequestBody AvisRequest request,
                                                @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, currentUser));
    }

    @GetMapping("/chauffeur/{chauffeurId}")
    public ResponseEntity<List<AvisResponse>> getByChauffeur(@PathVariable Long chauffeurId) {
        return ResponseEntity.ok(service.getByChauffeur(chauffeurId));
    }

    @GetMapping
    public ResponseEntity<List<AvisResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}