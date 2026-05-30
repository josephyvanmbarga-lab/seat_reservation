package com.mbarga.seat_reservation.notification;

import com.mbarga.seat_reservation.auth.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMesNotifications(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.getMesNotifications(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PatchMapping("/{id}/lu")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id,
                                                            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(service.markAsRead(id, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal User currentUser) {
        service.delete(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}