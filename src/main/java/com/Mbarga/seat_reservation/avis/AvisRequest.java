package com.mbarga.seat_reservation.avis;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvisRequest {

    @NotNull(message = "L'identifiant du trajet est obligatoire")
    private Long trajetId;

    @Min(value = 1, message = "La note minimale est 1")
    @Max(value = 5, message = "La note maximale est 5")
    private int note;

    private String commentaire;
}
