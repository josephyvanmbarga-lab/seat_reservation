package com.mbarga.seat_reservation.suivi;

import com.mbarga.seat_reservation.auth.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/suivi")
public class SuiviController {

    private final SuiviService           suiviService;
    private final SimpMessagingTemplate  messagingTemplate;

    public SuiviController(SuiviService suiviService, SimpMessagingTemplate messagingTemplate) {
        this.suiviService      = suiviService;
        this.messagingTemplate = messagingTemplate;
    }

    // ── REST ──────────────────────────────────────────────────────────────────

    @PostMapping("/lien/{reservationId}")
    public ResponseEntity<LienPartageResponse> genererLien(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(suiviService.genererLien(reservationId, currentUser));
    }

    @DeleteMapping("/lien/{token}")
    public ResponseEntity<Void> desactiverLien(
            @PathVariable String token,
            @AuthenticationPrincipal User currentUser) {
        suiviService.desactiverLien(token, currentUser);
        return ResponseEntity.noContent().build();
    }

    /** Accès public via lien partagé (pas de JWT requis) */
    @GetMapping("/{token}/positions")
    public ResponseEntity<List<PositionMessage>> getPositionsByToken(@PathVariable String token) {
        return ResponseEntity.ok(suiviService.getPositionsByToken(token));
    }

    @GetMapping("/trajet/{trajetId}/positions")
    public ResponseEntity<List<PositionMessage>> getPositionsByTrajet(@PathVariable Long trajetId) {
        return ResponseEntity.ok(suiviService.getPositionsByTrajet(trajetId));
    }

    // ── WebSocket STOMP ───────────────────────────────────────────────────────

    /**
     * Le client envoie sa position à /app/position/{trajetId}.
     * Le serveur la persiste et la diffuse à /topic/position/{trajetId}.
     */
    @MessageMapping("/position/{trajetId}")
    public void handlePosition(@DestinationVariable Long trajetId,
                               PositionMessage incoming,
                               Principal principal) {
        if (principal == null) return;

        User user = (User) ((Authentication) principal).getPrincipal();
        suiviService.updatePosition(user.getId(), trajetId,
                incoming.getLatitude(), incoming.getLongitude());

        PositionMessage broadcast = new PositionMessage(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                trajetId,
                incoming.getLatitude(),
                incoming.getLongitude(),
                OffsetDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/position/" + trajetId, broadcast);
    }
}