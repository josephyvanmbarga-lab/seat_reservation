package com.mbarga.seat_reservation.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPasswordRequest {

    @NotBlank(message = "L'ancien mot de passe est obligatoire")
    private String ancienMotDePasse;

    @NotBlank
    @Size(min = 6, message = "Le nouveau mot de passe doit contenir au moins 6 caractères")
    private String nouveauMotDePasse;
}
