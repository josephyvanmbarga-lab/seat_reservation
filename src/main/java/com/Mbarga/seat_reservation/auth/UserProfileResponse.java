package com.mbarga.seat_reservation.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {

    private Long   id;
    private String username;
    private String email;
    private String telephone;
    private String role;
    private Double noteMoyenne;
    private int    nbAvis;

    public static UserProfileResponse from(User u) {
        return new UserProfileResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getTelephone(),
                u.getRole().name(),
                u.getNoteMoyenne(),
                u.getNbAvis()
        );
    }
}
