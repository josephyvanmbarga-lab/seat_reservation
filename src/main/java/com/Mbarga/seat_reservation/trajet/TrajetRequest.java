package com.mbarga.seat_reservation.trajet;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class TrajetRequest {

    @NotNull(message = "L'identifiant du véhicule est obligatoire")
    private Long vehiculeId;

    @NotBlank(message = "Le point de départ est obligatoire")
    private String pointDepart;

    @NotNull(message = "La latitude de départ est obligatoire")
    private BigDecimal latDepart;

    @NotNull(message = "La longitude de départ est obligatoire")
    private BigDecimal lngDepart;

    @NotBlank(message = "Le point d'arrivée est obligatoire")
    private String pointArrivee;

    @NotNull(message = "La latitude d'arrivée est obligatoire")
    private BigDecimal latArrivee;

    @NotNull(message = "La longitude d'arrivée est obligatoire")
    private BigDecimal lngArrivee;

    @NotNull(message = "La date et heure de départ sont obligatoires")
    @Future(message = "La date de départ doit être dans le futur")
    private OffsetDateTime dateHeureDepart;
}
