package com.mbarga.seat_reservation.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long   userId;
    private String username;
    private String role;
    private String token;
}
