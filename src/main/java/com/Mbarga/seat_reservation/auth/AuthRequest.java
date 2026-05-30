package com.mbarga.seat_reservation.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
