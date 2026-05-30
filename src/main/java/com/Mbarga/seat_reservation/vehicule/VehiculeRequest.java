package com.mbarga.seat_reservation.vehicule;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehiculeRequest {

    @NotBlank(message = "L'immatriculation est obligatoire")
    private String immatriculation;

    private String modele;

    @Min(value = 1, message = "La capacité doit être d'au moins 1 siège")
    private int capacite;

    @NotNull(message = "Le type de disposition est obligatoire")
    private TypeDisposition typeDisposition;
}
