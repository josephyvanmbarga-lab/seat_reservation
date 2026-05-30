package com.mbarga.seat_reservation.trajet;

import com.mbarga.seat_reservation.auth.User;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trajets")
public class TrajetController {

    private final TrajetService service;

    public TrajetController(TrajetService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TrajetResponse> create(@Valid @RequestBody TrajetRequest request,
                                                  @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrajetResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<TrajetResponse>> search(
            @RequestParam(required = false) String pointDepart,
            @RequestParam(required = false) String pointArrivee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime date) {
        return ResponseEntity.ok(service.search(pointDepart, pointArrivee, date));
    }

    @GetMapping("/mes-trajets")
    public ResponseEntity<List<TrajetResponse>> getMesTrajets(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getMesTrajets(currentUser));
    }

    @GetMapping("/historique")
    public ResponseEntity<List<TrajetResponse>> getHistorique(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getHistorique(currentUser));
    }

    @GetMapping("/{id}/sieges")
    public ResponseEntity<List<SiegePlanResponse>> getPlanSieges(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPlanSieges(id));
    }

    @GetMapping("/estimation")
    public ResponseEntity<EstimationResponse> estimer(
            @RequestParam BigDecimal latDepart,
            @RequestParam BigDecimal lngDepart,
            @RequestParam BigDecimal latArrivee,
            @RequestParam BigDecimal lngArrivee) {
        return ResponseEntity.ok(service.estimer(latDepart, lngDepart, latArrivee, lngArrivee));
    }

    @PatchMapping("/{id}/demarrer")
    public ResponseEntity<TrajetResponse> demarrer(@PathVariable Long id,
                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.demarrer(id, currentUser));
    }

    @PatchMapping("/{id}/terminer")
    public ResponseEntity<TrajetResponse> terminer(@PathVariable Long id,
                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.terminer(id, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TrajetResponse> annuler(@PathVariable Long id,
                                                   @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.annuler(id, currentUser));
    }
}
